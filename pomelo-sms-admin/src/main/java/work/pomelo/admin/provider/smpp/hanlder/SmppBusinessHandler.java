package work.pomelo.admin.provider.smpp.hanlder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zx.sms.codec.smpp.msg.*;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import work.pomelo.admin.domain.SmsCollect;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.enums.SmsEnum;
import work.pomelo.admin.mapper.SmsCollectMapper;
import work.pomelo.admin.mapper.SmsDetailsMapper;
import work.pomelo.common.redis.service.RedisPrefix;
import work.pomelo.common.redis.service.RedisService;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
@Slf4j
public class SmppBusinessHandler extends AbstractBusinessHandler {
    private RedisService redisService;
    private SmsDetailsMapper smsDetailsMapper;
    private SmsCollectMapper smsCollectMapper;

    public SmppBusinessHandler(RedisService redisService, SmsDetailsMapper smsDetailsMapper, SmsCollectMapper smsCollectMapper) {
        this.redisService = redisService;
        this.smsDetailsMapper = smsDetailsMapper;
        this.smsCollectMapper = smsCollectMapper;
    }

    @Override
    public String name() {
        return "custom-smpp-business-handler";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        EndpointEntity entity = getEndpointEntity();
//        log.info("收到SMSC请求, 交由业务处理 ===> entityId: {}, 信息类型: {}, 信息详情: {}",
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
                    log.info("下行短信状态报告 ===> 状态报告ID: {}, phone: {}, reportStat: {}", id, address, reportStat);
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

                    SmsDetails details = redisService.getCacheObject(RedisPrefix.RESP + id);
                    if (details != null) {
                        SmsCollect collect = redisService.getCacheObject(RedisPrefix.COLLECT + details.getCollectId());
                        log.info("handler details: " + details.toString() + " , collect: " + collect.toString());
                        SmsCollect set = new SmsCollect();
                        if (collect.getPendingNum() > 0) {
                            set.setPendingNum(collect.getPendingNum() - 1);
                            if (collect.getPendingNum() == 1) {
                                set.setStatus(SmsEnum.SUCCESS.getStatus());
                            }
                        }
                        if (sendStatus == 2) {
                            log.info("回调更新, id: {} 发送中", collect.getId());
                            // 向 SMSC 发送短信已送达响应信息
                            DeliverSmResp deliverSmResp = deliverSm.createResponse();
                            ctx.channel().writeAndFlush(deliverSmResp);
                            return;
                        } else if (sendStatus == 10) {
                            set.setSuccessNum(collect.getSuccessNum() + 1);
                        } else {
                            set.setFailNum(collect.getFailNum() + 1);
                        }

                        // 删除短信详情缓存
                        redisService.deleteObject(RedisPrefix.RESP + id);

                        log.info("回调更新汇总, id: {}, set: {}", collect.getId(), set);
                        smsCollectMapper.update(set, new QueryWrapper<SmsCollect>().eq("id", collect.getId()));
                        if (Objects.nonNull(set.getStatus()))
                            collect.setStatus(set.getStatus());
                        if (Objects.nonNull(set.getPendingNum()))
                            collect.setPendingNum(set.getPendingNum());
                        if (Objects.nonNull(set.getSuccessNum()))
                            collect.setSuccessNum(set.getSuccessNum());
                        if (Objects.nonNull(set.getFailNum()))
                            collect.setFailNum(set.getFailNum());

                        redisService.setCacheObject(RedisPrefix.COLLECT + details.getCollectId(), collect, 1, TimeUnit.DAYS);
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
                log.info("SMSC SubmitSm 消息响应, 目的地号码: {}, 短信ID: {}, respID: {}", msisdn, messageId, respMessageId);

                SmsDetails smsDetails = redisService.getCacheObject(RedisPrefix.DETAIL + messageId);
                if (smsDetails != null) {
                    int sendStatus = "OK".equals(submitSmResp.getResultMessage()) ? 3 : -1;
                    SmsDetails updateEntity = new SmsDetails();
                    updateEntity.setStatus(sendStatus);
                    updateEntity.setRespMessageId(respMessageId);
                    smsDetailsMapper.update(updateEntity, new QueryWrapper<SmsDetails>().eq("details_id", messageId));

                    // 添加缓存
                    smsDetails.setStatus(sendStatus);
                    smsDetails.setRespMessageId(respMessageId);
                    if (!StringUtils.isEmpty(respMessageId)) {
                        redisService.setCacheObject(RedisPrefix.RESP + respMessageId, smsDetails, 1, TimeUnit.DAYS);
                        // 删除重复
                        redisService.deleteObject(RedisPrefix.DETAIL + messageId);
                        return;
                    }
                    log.warn("不存在发送响应ID, 短信ID: {}", messageId);
                }
            }
        } catch (Exception e) {
            log.error("短信业务处理异常, {}", ExceptionUtils.getStackTrace(e));
        }
        ctx.fireChannelRead(msg);
    }
}
