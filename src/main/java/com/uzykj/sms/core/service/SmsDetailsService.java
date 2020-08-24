package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.domain.dto.SmsDetailsDto;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.mapper.SysUserMapper;
import com.uzykj.sms.core.common.json.JsonResult;
import com.uzykj.sms.core.util.DateUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
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
    private SmsAccountService smsAccountService;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Transactional(rollbackFor = Exception.class)
    public JsonResult processSmsList(List<String> phoneList, String content, SysUser user) {
        long startTime = System.currentTimeMillis();
        String collectId = UUID.randomUUID().toString();
        try {
            SmsCollect collect = new SmsCollect();
            collect.setCollectId(collectId);
            collect.setUserId(user.getId());
            collect.setContents(content);
            collect.setTotal(phoneList.size());
            collect.setPendingNum(phoneList.size());
            collect.setStatus(SmsEnum.PENDING.getStatus());

            log.info("添加汇总记录：" + collect.toString());
            smsCollectMapper.insert(collect);
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
                insert(children, user, content, collectId, batchNo);
            }
            log.info("批量短信使用时间：" + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            log.log(Level.WARNING, "批量添加短信详情错误", e);
            return JsonResult.toError("批量添加短信详情错误");
        }
        return new JsonResult<>();
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(List<String> phoneList, SysUser user, String content, String collectId, String batchNo) {
        //批量添加
        phoneList.forEach(phone -> {
            SmsDetails d = new SmsDetails();
            d.setDetailsId(UUID.randomUUID().toString());
            d.setCollectId(collectId);
            d.setContents(content);
            d.setBatchId(batchNo);
            d.setUserId(user.getId());
            d.setPhone(phone);
            d.setStatus(1);
            // 下行短信
            d.setDirection(2);

            SmsAccount smsAccount = smsAccountService.get(user.getAccountId());
            d.setAccountCode(smsAccount.getCode());

            smsDetailsMapper.insert(d);
        });
        log.info("批量添加短信详情：" + phoneList.size() + "条");
    }

    @Transactional(rollbackFor = Exception.class)
    public void insert(String phone, SysUser user, String content, String collectId, String batchNo) {
        SmsDetails d = new SmsDetails();
        d.setDetailsId(UUID.randomUUID().toString());
        d.setCollectId(collectId);
        d.setContents(content);
        d.setBatchId(batchNo);
        d.setUserId(user.getId());
        d.setPhone(phone);
        d.setStatus(1);
        // 下行短信
        d.setDirection(2);

        SysUser sysUser = Globle.USER_CACHE.get(user.getId());
        d.setAccountCode(sysUser.getAccount().getCode());

        smsDetailsMapper.insert(d);
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
