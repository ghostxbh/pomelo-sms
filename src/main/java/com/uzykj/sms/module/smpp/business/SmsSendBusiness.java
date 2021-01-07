package com.uzykj.sms.module.smpp.business;


import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.redis.service.RedisService;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
@Slf4j
@NoArgsConstructor
public class SmsSendBusiness {
    private static Logger logger = LoggerFactory.getLogger(SmsSendBusiness.class);
    private static RedisService redisService = ApplicationContextUtil.getApplicationContext().getBean(RedisService.class);
    private final EndpointManager manager = EndpointManager.INS;
    private String code;
    private SmsDetails details;

    public SmsSendBusiness(String code, SmsDetails details) {
        this.code = code;
        this.details = details;
    }

    public void send() {
        if (StringUtils.isAnyBlank(details.getContents()) || StringUtils.isAnyBlank(details.getPhone())) {
            throw new RuntimeException("参数有误");
        }
        EndpointEntity entity = manager.getEndpointEntity(code);
        if (entity == null) {
            logger.warn("smpp链接异常，code: " + code);
            throw new RuntimeException("smpp链接异常");
        }
        String batchId = details.getBatchId();
        String key = batchId + details.getPhone();
        Object cacheObject = redisService.getCacheObject(0, key);
        if (cacheObject != null) {
            return;
        } else {
            redisService.setCacheObject(0, key, 1, 2, TimeUnit.HOURS);
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
            log.info("send sms obj: {}} ", submitSm);
            Object obj = redisService.getCacheObject(0, batchId);
            if (obj != null)
                redisService.setCacheObject(0, batchId, Integer.parseInt(obj.toString()) + 1, 4, TimeUnit.HOURS);
            else
                redisService.setCacheObject(0, batchId, 1, 4, TimeUnit.HOURS);
            ChannelUtil.asyncWriteToEntity(entity.getId(), submitSm);
        } catch (Exception e) {
            logger.error("发送短信异常", e);
        }
    }
}
