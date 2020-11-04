package com.uzykj.sms.module.smpp.business;


import com.uzykj.sms.core.domain.SmsDetails;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
@NoArgsConstructor
public class SmsSendBusiness implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(SmsSendBusiness.class);
    private final EndpointManager manager = EndpointManager.INS;
    private String code;
    private SmsDetails details;

    public SmsSendBusiness(String code, SmsDetails details) {
        this.code = code;
        this.details = details;
    }

    public void send(String code, SmsDetails details) {
        if (StringUtils.isAnyBlank(details.getContents()) || StringUtils.isAnyBlank(details.getPhone())) {
            throw new RuntimeException("参数有误");
        }
        EndpointEntity entity = manager.getEndpointEntity(code);
        if (entity == null) {
            logger.warn("smpp链接异常，code: " + code);
            throw new RuntimeException("smpp链接异常");
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
    }

    @Override
    public void run() {
        logger.info("正在发送短信，号码 {}", details.getPhone());
        try {
            this.send(code, details);
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            logger.error("发送短信异常", e);
        }
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 9999 ; i++) {
            String phone = "1371010";
            StringBuilder value = new StringBuilder(String.valueOf(i));
            int length = 4 - value.length();
            if (length == 1)
                value.insert(0, "0");
            else if (length == 2)
                value.insert(0, "00");
            else if (length == 3)
                value.insert(0, "000");
            System.out.println(phone + new String(value));
        }
    }
}
