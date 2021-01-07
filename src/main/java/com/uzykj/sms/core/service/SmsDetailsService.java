package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.common.json.JsonResult;
import com.uzykj.sms.core.common.redis.service.RedisService;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.domain.dto.SmsDetailsDto;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.mapper.SysUserMapper;
import com.uzykj.sms.core.util.DateUtils;
import com.uzykj.sms.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SmsDetailsService {
    private static Logger log = Logger.getLogger(SmsDetailsService.class.getName());
    @Autowired
    private SmsDetailsMapper smsDetailsMapper;
    @Autowired
    private SmsCollectMapper smsCollectMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedisService redisService;

    @Async
    @Transactional(rollbackFor = Exception.class)
    public JsonResult<?> processSmsList(List<String> phoneList, String content, SysUser user) {
        long startTime = System.currentTimeMillis();
        String collectId = UUID.randomUUID().toString();
        try {
            SysUser sysUser = Globle.USER_CACHE.get(user.getId());
            SmsCollect collect = new SmsCollect();
            collect.setCollectId(collectId);
            collect.setUserId(user.getId());
            collect.setAccountCode(sysUser.getAccount().getCode());
            collect.setContents(content);
            collect.setTotal(phoneList.size());
            collect.setPendingNum(phoneList.size());
            collect.setStatus(SmsEnum.PENDING.getStatus());
            collect.setSuccessNum(0);
            collect.setFailNum(0);

            log.info("添加汇总记录：" + collect.toString());
            smsCollectMapper.insert(collect);

            redisService.setCacheObject(1, collect.getCollectId(), collect, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            log.log(Level.WARNING, "添加汇总记录错误", e);
            return JsonResult.toError("添加汇总记录错误");
        }

        try {
            SysUser set = new SysUser();
            if (phoneList.size() > user.getAllowance()) {
                return JsonResult.toError("余额不足");
            }
            set.setAllowance(user.getAllowance() - phoneList.size());
            log.info("扣除发送余额：" + set.toString());

            sysUserMapper.update(set, new QueryWrapper<SysUser>().eq("id", user.getId()));
        } catch (Exception e) {
            log.log(Level.WARNING, "扣除发送余额错误", e);
            return JsonResult.toError("扣除发送余额错误");
        }

        try {
            String batchNo = DateUtils.getBatchNo();
            for (String children : phoneList) {
                if (!StringUtils.isEmpty(children.trim())) {
                    String setContent;
                    if (user.getTextSuffix() > 0) {
                        setContent = content + "。" + StringUtil.getVercode("ULN", 3);
                    } else {
                        setContent = content;
                    }

                    if (!"86".equals(children.substring(0, 2))) {
                        String phonePrefix = !StringUtils.isEmpty(user.getPhonePrefix().trim()) ? user.getPhonePrefix().trim() : "86";
                        children = phonePrefix + children;
                    }

                    SysUser sysUser = Globle.USER_CACHE.get(user.getId());
                    SmsDetails details = SmsDetails.builder()
                            .detailsId(UUID.randomUUID().toString())
                            .collectId(collectId)
                            .contents(setContent)
                            .batchId(batchNo)
                            .userId(user.getId())
                            .phone(children)
                            // 下行短信
                            .status(1)
                            .direction(2)
                            .accountCode(sysUser.getAccount().getCode())
                            .build();
                    smsDetailsMapper.insert(details);

                    redisService.setCacheObject(2, details.getDetailsId(), details, 1, TimeUnit.DAYS);
                }
            }
            log.info("批量短信使用时间：" + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            log.log(Level.WARNING, "批量添加短信详情错误", e);
            return JsonResult.toError("批量添加短信详情错误");
        }
        return new JsonResult<>();
    }

    public int currentCount(int userId) {
        return smsDetailsMapper.currentCount(userId);
    }

    public List<SmsDetails> getList(SmsDetailsDto smsDetailsDto) {
        return smsDetailsMapper.getList(smsDetailsDto);
    }

    public int getListCount(SmsDetailsDto smsDetailsDto) {
        return smsDetailsMapper.getListCount(smsDetailsDto);
    }
}
