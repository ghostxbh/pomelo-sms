package com.uzykj.smsSystem.module.smpp.business;


import com.uzykj.smsSystem.core.common.Globle;
import com.uzykj.smsSystem.core.domain.SmsDetails;
import com.uzykj.smsSystem.core.util.DateUtils;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
public class SmsSendBusiness extends Globle {
    private static Logger logger = LoggerFactory.getLogger(SmsSendBusiness.class);
    private final EndpointManager manager = EndpointManager.INS;

    public void batchSend(String code, List<SmsDetails> detailsList) throws Exception {
        logger.info("短信批量发送， 共 " + detailsList.size() + " 条");
        String batchNo = DateUtils.getBatchNo();
        for (SmsDetails details : detailsList) {
            details.setBatchId(batchNo);
            send(code, details);
        }
    }

    public void send(String code, SmsDetails details) throws Exception {
        if (StringUtils.isAnyBlank(details.getContents()) || StringUtils.isAnyBlank(details.getPhone())) {
            throw new RuntimeException("参数有误");
        }
        EndpointEntity entity = manager.getEndpointEntity(code);
        if (entity == null) {
            logger.warn("smpp链接异常，code: " + code);
            return;
        }

        SubmitSm submitSm = new SubmitSm();
        submitSm.setReferenceObject(details.getDetailsId());
        submitSm.setSmsMsg(details.getContents());

        Address sourceAddress = new Address();
        sourceAddress.setTon((byte) 5);
        sourceAddress.setNpi((byte) 0);
        sourceAddress.setAddress("86");

        Address destAddress = new Address();
        destAddress.setAddress(details.getPhone());
        submitSm.setDestAddress(destAddress);
        // 送达报告
        submitSm.setRegisteredDelivery((byte) 1);
        logger.info("待发送短信 sequenceNo: {}", submitSm.getSequenceNo());
        ChannelUtil.syncWriteLongMsgToEntity(entity.getId(), submitSm);
    }
}
