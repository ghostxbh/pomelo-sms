package com.uzykj.sms.module.smpp.hanlder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.*;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
public class SmppBusinessHandler extends AbstractBusinessHandler {

    private static Logger logger = LoggerFactory.getLogger(SmppBusinessHandler.class);

    private SmsDetailsMapper smsDetailsMapper;

    public SmppBusinessHandler(SmsDetailsMapper smsDetailsMapper) {
        this.smsDetailsMapper = smsDetailsMapper;
    }

    @Override
    public String name() {
        return "custom-smpp-business-handler";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        EndpointEntity entity = getEndpointEntity();

        logger.info("收到SMSC请求, 交由业务处理 ===> entityId: {}, 信息类型: {}, 信息详情: {}",
                entity.getId(), msg.getClass(), msg);
        // DeliverSm: SMSC -> ESME, 对平台而言即上行短信
        try {
            if (msg instanceof DeliverSm) {
                DeliverSm deliverSm = (DeliverSm) msg;

                // 不是状态报告, 也即上行短信
                if (!deliverSm.isReport()) {
                    String msgContent = deliverSm.getMsgContent();
                    Address sourceAddress = deliverSm.getSourceAddress();

                    String sourceAddressVal = sourceAddress.getAddress();
                    String msisdn = sourceAddressVal.substring(1);

                    logger.info("上行短信内容: {}, 源地址: {npi: {}, ton: {}, address: {}}",
                            msgContent, sourceAddress.getNpi(), sourceAddress.getTon(), msisdn);

                    SmsDetails smsRecord = new SmsDetails();
                    String detailsId = UUID.randomUUID().toString();
                    smsRecord.setDetailsId(detailsId);
                    smsRecord.setContents(msgContent);
                    smsRecord.setDirection(1);
                    smsRecord.setAccountCode(entity.getId());
                    smsRecord.setReceiveTime(new Date());
                    smsRecord.setStatus(10);
                    smsDetailsMapper.insert(smsRecord);
                }
                // 状态报告
                else {
                    DeliverSmReceipt deliverSmReceipt = (DeliverSmReceipt) msg;
                    String address = deliverSmReceipt.getDestAddress().getAddress();
                    String reportStat = deliverSmReceipt.getStat();
                    logger.info("下行短信状态报告 ===> phone: {}, reportStat: {}", address, reportStat);
                    String id = deliverSmReceipt.getId();
                    logger.info("状态报告ID: {}", id);
                    int sendStatus = -1;
                    // 送达
                    // 送达报告状态非 DELIVRD 一律认定为发送失败
                    if ("DELIVRD".equals(reportStat)) {
                        sendStatus = 10;
                    }
                    SmsDetails updateEntity = new SmsDetails();
                    updateEntity.setReportStat(reportStat);
                    updateEntity.setStatus(sendStatus);
                    smsDetailsMapper.update(updateEntity, new QueryWrapper<SmsDetails>().eq("resp_message_id", id));

                    SmsDetails details = smsDetailsMapper.selectOne(new QueryWrapper<SmsDetails>().eq("resp_message_id", id));
                    SmsCollect collect = Globle.smsCollectMapper.selectOne(new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));

                    SmsCollect set = new SmsCollect();
                    if (collect.getPendingNum() > 0) {
                        set.setPendingNum(collect.getPendingNum() - 1);
                        if (collect.getPendingNum() == 1) {
                            set.setStatus(SmsEnum.SUCCESS.getStatus());
                        }
                    }
                    if (sendStatus == 10) {
                        set.setSuccessNum(collect.getSuccessNum() + 1);
                    } else {
                        set.setFailNum(collect.getFailNum() + 1);
                    }
                    logger.info("回调更新汇总, id: {}, set: {}", collect.getId(), set);
                    Globle.smsCollectMapper.update(set, new QueryWrapper<SmsCollect>().eq("id", collect.getId()));
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
                logger.info("SubmitSmResp messageId: {}", respMessageId);
                String msisdn = request.getDestAddress().getAddress();
                String messageId = (String) request.getReferenceObject();
                logger.info("SMSC SubmitSm 消息响应, msisdn: {}, messageId: {}", msisdn, messageId);
                SmsDetails smsDetails = smsDetailsMapper.selectOne(new QueryWrapper<SmsDetails>().eq("details_id", messageId));
                if (smsDetails != null) {
                    int sendStatus = "OK".equals(submitSmResp.getResultMessage()) ? 3 : -1;
                    SmsDetails updateEntity = new SmsDetails();
                    updateEntity.setStatus(sendStatus);
                    updateEntity.setRespMessageId(respMessageId);
                    smsDetailsMapper.update(updateEntity, new QueryWrapper<SmsDetails>().eq("details_id", messageId));
                }
            }
        } catch (Exception e) {
            logger.error("短信业务处理异常, {}", ExceptionUtils.getStackTrace(e));
        }
        ctx.fireChannelRead(msg);
    }
}
