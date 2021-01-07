package com.uzykj.sms.core.controller.api;

import com.uzykj.sms.core.common.json.JsonResult;
import com.uzykj.sms.core.common.redis.service.RedisService;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.dto.SmsSendDTO;
import com.uzykj.sms.core.enums.ResponseCode;
import com.uzykj.sms.core.util.DateUtils;
import com.uzykj.sms.module.smpp.business.SmsSendBusiness;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.OpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
@RestController
@RequestMapping("/sms")
public class SmsSmppController {

    @Autowired
    private RedisService redisService;

    @PostMapping("/send")
    public JsonResult<String> test(@RequestBody SmsSendDTO smsSendDTO) {
        if (smsSendDTO == null) {
            return JsonResult.error(ResponseCode.PARAM_EMPTY);
        }
        String shortCode = smsSendDTO.getShortCode();
        List<String> mobiles = smsSendDTO.getMobiles();
        String content = smsSendDTO.getContent();
        if (StringUtils.isAnyBlank(shortCode, content) || CollectionUtils.isEmpty(mobiles)) {
            return JsonResult.error(ResponseCode.PARAM_EMPTY);
        }
        try {

            String batchNo = DateUtils.getBatchNo();
            String code = smsSendDTO.getShortCode();
            Optional.ofNullable(smsSendDTO.getMobiles())
                    .orElse(new ArrayList<String>(0))
                    .forEach(p -> {
                        SmsDetails d = new SmsDetails();
                        d.setBatchId(batchNo);
                        d.setContents(smsSendDTO.getContent());
                        d.setDetailsId(UUID.randomUUID().toString());
                        d.setPhone(p);
                        d.setCreateTime(new Date());
                        d.setDirection(2);
                        d.setStatus(1);
                        d.setAccountCode(code);
                        d.setUserId(smsSendDTO.getUserId());
                        SmsSendBusiness business = new SmsSendBusiness(code, d);
                        business.send();
                    });
            return new JsonResult();
        } catch (Exception e) {
            return JsonResult.error(ResponseCode.PARAM_ERROR);
        }
    }

    @GetMapping("/redis")
    public JsonResult<String> testRedis(@RequestParam String name) {
        redisService.setCacheObject(0, "test", name, 20, TimeUnit.SECONDS);
        redisService.setCacheObject(1, "test", name, 20, TimeUnit.SECONDS);
        redisService.setCacheObject(2, "test", name, 20, TimeUnit.SECONDS);
        return new JsonResult(name);
    }
}
