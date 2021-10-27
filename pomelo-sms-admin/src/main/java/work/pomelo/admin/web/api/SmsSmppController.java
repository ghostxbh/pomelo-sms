package work.pomelo.admin.web.api;

import com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.*;
import work.pomelo.admin.common.json.JsonResult;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.domain.dto.SmsSendDTO;
import work.pomelo.admin.enums.ResponseCode;
import work.pomelo.admin.provider.smpp.business.SmsSendBusiness;
import work.pomelo.admin.utils.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import work.pomelo.common.redis.configure.RedisDBChangeUtil;
import work.pomelo.common.redis.service.RedisService;

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
    @Autowired
    private RedisDBChangeUtil redisDBChangeUtil;

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

    @GetMapping("/redis/set")
    public JsonResult<String> getSetRedis(@RequestParam String key, @RequestParam String value) {
        redisDBChangeUtil.setDataBase(15);
        redisService.setCacheObject(key, value, 2, TimeUnit.MINUTES);
        redisDBChangeUtil.setDataBase(14);
        redisService.setCacheObject(key, value, 2, TimeUnit.MINUTES);
        redisDBChangeUtil.setDataBase(13);
        redisService.setCacheObject(key, value, 2, TimeUnit.MINUTES);

        HashMap<String, String> map = Maps.newHashMap();
        map.put("key", key);
        map.put("value", value);
        return new JsonResult(map);
    }

    @PostMapping("/redis/set")
    public JsonResult<String> postSetRedis(@RequestBody Map<String, Object> params) {
        redisDBChangeUtil.setDataBase(15);
        redisService.setCacheObject(params.get("key").toString(), params.get("value"), 2, TimeUnit.MINUTES);
        redisDBChangeUtil.setDataBase(14);
        redisService.setCacheObject(params.get("key").toString(), params.get("value"), 2, TimeUnit.MINUTES);
        redisDBChangeUtil.setDataBase(13);
        redisService.setCacheObject(params.get("key").toString(), params.get("value"), 2, TimeUnit.MINUTES);
        return new JsonResult(params);
    }

    @GetMapping("/redis/get")
    public JsonResult<String> getRedis(@RequestParam String key) {
        redisDBChangeUtil.setDataBase(15);
        Object value1 = redisService.getCacheObject(key);
        redisDBChangeUtil.setDataBase(14);
        Object value2 = redisService.getCacheObject(key);
        redisDBChangeUtil.setDataBase(13);
        Object value3 = redisService.getCacheObject(key);

        List<Object> list = Lists.newArrayList();
        list.add(key);
        list.add(value1);
        list.add(value2);
        list.add(value3);
        return new JsonResult(list);
    }
}
