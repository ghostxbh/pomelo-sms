package com.uzykj.sms.module.smpp.business;


import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.util.DateUtils;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
public class SmsSendBusiness extends Globle {
    private static Logger logger = LoggerFactory.getLogger(SmsSendBusiness.class);
    private final EndpointManager manager = EndpointManager.INS;

    @Async
    public void batchSend(String code, List<SmsDetails> detailsList) {
        logger.info("短信批量发送， 共 " + detailsList.size() + " 条");
        detailsList.forEach(detail -> send(code, detail));
    }

    public void send(String code, SmsDetails details) {
        long millis = System.currentTimeMillis();
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
        submitSm.setSourceAddress(sourceAddress);

        // 送达报告
        submitSm.setRegisteredDelivery((byte) 1);
        logger.info("待发送短信 sequenceNo: {}", submitSm.getSequenceNo());
        try {
            ChannelUtil.asyncWriteToEntity(entity.getId(), submitSm);
        } catch (Exception e) {
            logger.error("发送短信异常", e);
        }
        logger.info("单条短信耗时：{}ms", (System.currentTimeMillis() - millis));
    }

    private String parsePhone(List<SmsDetails> detailsList) {
        return Optional.ofNullable(detailsList)
                .orElse(new ArrayList<SmsDetails>(0))
                .stream()
                .map(SmsDetails::getPhone)
                .collect(Collectors.joining(","));
    }
}
