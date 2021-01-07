package com.uzykj.sms.module.smpp.hanlder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.redis.service.RedisService;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.zx.sms.codec.smpp.msg.*;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
public class SmppBusinessHandler extends AbstractBusinessHandler {
    private static Logger logger = LoggerFactory.getLogger(SmppBusinessHandler.class);
    private RedisService redisService = ApplicationContextUtil.getApplicationContext().getBean(RedisService.class);
    private SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);

    @Override
    public String name() {
        return "custom-smpp-business-handler";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        EndpointEntity entity = getEndpointEntity();
//        logger.info("收到SMSC请求, 交由业务处理 ===> entityId: {}, 信息类型: {}, 信息详情: {}",
//                entity.getId(), msg.getClass(), msg);
        // DeliverSm: SMSC -> ESME, 对平台而言即上行短信
        try {
            if (msg instanceof DeliverSm) {
                DeliverSm deliverSm = (DeliverSm) msg;
                // 不是状态报告, 也即上行短信
                if (!deliverSm.isReport()) {
                    // TODO 上行短信 不做逻辑处理
                }
                // 状态报告
                else {
                    DeliverSmReceipt deliverSmReceipt = (DeliverSmReceipt) msg;
                    String address = deliverSmReceipt.getSourceAddress().getAddress();
                    String reportStat = deliverSmReceipt.getStat();
                    String id = deliverSmReceipt.getId();
                    logger.info("下行短信状态报告 ===> 状态报告ID: {}, phone: {}, reportStat: {}", id, address, reportStat);
                    int sendStatus = -1;
                    // 送达报告状态非 DELIVRD 一律认定为发送失败
                    if ("DELIVRD".equalsIgnoreCase(reportStat)) {
                        sendStatus = 10;
                    } else if ("SUBMITTED".equalsIgnoreCase(reportStat)) {
                        sendStatus = 2;
                    }
                    SmsDetails updateEntity = new SmsDetails();
                    updateEntity.setReportStat(reportStat);
                    updateEntity.setStatus(sendStatus);
                    smsDetailsMapper.update(updateEntity, new QueryWrapper<SmsDetails>().eq("resp_message_id", id));

                    SmsDetails details = redisService.getCacheObject(2, id);
                    if (details != null) {
                        SmsCollect collect = redisService.getCacheObject(1, details.getCollectId());
                        logger.info("handler details: " + details.toString() + " , collect: " + collect.toString());
                        SmsCollect set = new SmsCollect();
                        if (collect.getPendingNum() > 0) {
                            set.setPendingNum(collect.getPendingNum() - 1);
                            if (collect.getPendingNum() == 1) {
                                set.setStatus(SmsEnum.SUCCESS.getStatus());
                            }
                        }
                        if (sendStatus == 2) {
                            logger.info("回调更新, id: {} 发送中", collect.getId());
                            // 向 SMSC 发送短信已送达响应信息
                            DeliverSmResp deliverSmResp = deliverSm.createResponse();
                            ctx.channel().writeAndFlush(deliverSmResp);
                            return;
                        } else if (sendStatus == 10) {
                            set.setSuccessNum(collect.getSuccessNum() + 1);
                        } else {
                            set.setFailNum(collect.getFailNum() + 1);
                        }
                        logger.info("回调更新汇总, id: {}, set: {}", collect.getId(), set);
                        smsCollectMapper.update(set, new QueryWrapper<SmsCollect>().eq("id", collect.getId()));

                        if (Objects.nonNull(set.getStatus()))
                            collect.setStatus(set.getStatus());
                        if (Objects.nonNull(set.getPendingNum()))
                            collect.setPendingNum(set.getPendingNum());
                        if (Objects.nonNull(set.getSuccessNum()))
                            collect.setSuccessNum(set.getSuccessNum());
                        if (Objects.nonNull(set.getFailNum()))
                            collect.setFailNum(set.getFailNum());
                        redisService.setCacheObject(1, details.getCollectId(), collect, 1, TimeUnit.DAYS);
                    }
                }

                // 向 SMSC 发送短信已送达响应信息
                DeliverSmResp deliverSmResp = deliverSm.createResponse();
                ctx.channel().writeAndFlush(deliverSmResp);
            }
            // SubmitSm: ESME -> SMSC, 对平台而言即下行短信
            // SubmitSmResp 即 SMSC 回复 ESME 的 SubmitSm 响应
            else if (msg instanceof SubmitSmResp) {
                SubmitSmResp submitSmResp = (SubmitSmResp) msg;
                SubmitSm request = (SubmitSm) submitSmResp.getRequest();
                String respMessageId = submitSmResp.getMessageId();
                String msisdn = request.getDestAddress().getAddress();
                String messageId = (String) request.getReferenceObject();
                logger.info("SMSC SubmitSm 消息响应, 目的地号码: {}, 短信ID: {}", msisdn, messageId);

                SmsDetails smsDetails = redisService.getCacheObject(2, messageId);
                if (smsDetails != null) {
                    int sendStatus = "OK".equals(submitSmResp.getResultMessage()) ? 3 : -1;
                    SmsDetails updateEntity = new SmsDetails();
                    updateEntity.setStatus(sendStatus);
                    updateEntity.setRespMessageId(respMessageId);
                    smsDetailsMapper.update(updateEntity, new QueryWrapper<SmsDetails>().eq("details_id", messageId));

                    // 添加缓存
                    smsDetails.setStatus(sendStatus);
                    smsDetails.setRespMessageId(respMessageId);
                    redisService.setCacheObject(2, respMessageId, smsDetails, 1, TimeUnit.DAYS);
                    // 删除重复
                    redisService.deleteObject(2, messageId);
                }
            }
        } catch (Exception e) {
            logger.error("短信业务处理异常, {}", ExceptionUtils.getStackTrace(e));
        }
        ctx.fireChannelRead(msg);
    }
}
