package com.uzykj.sms.module.http.ebulk;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.http.HttpClient4;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EbulkSend implements Runnable {
    private static Logger log = Logger.getLogger(EbulkSend.class.getName());
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    private SmsDetails details;
    private SmsAccount account;

    public EbulkSend(SmsDetails details, SmsAccount account) {
        this.details = details;
        this.account = account;
    }

    public void submitMessage(SmsDetails details, SmsAccount account) {
        Map<String, Object> paramMap = new HashMap<String, Object>(6);
        paramMap.put("username", account.getSystemId());
        paramMap.put("password", account.getPassword());
        paramMap.put("type", "2");
        paramMap.put("sid", "855");
        paramMap.put("message", details.getContents());
        paramMap.put("dcs", "0");
        paramMap.put("mno", details.getPhone());
        try {
            log.info("回调请求数据 url: " + account.getUrl() + ", 参数：" + paramMap);
            String res = HttpClient4.doPost(account.getUrl(), paramMap);
            log.info("回调数据打印：" + res);
            boolean isSendSuccess = true;
            if (res.contains("Message")) {
                isSendSuccess = false;
            }
            SmsDetails set = new SmsDetails();
            if (isSendSuccess) {
                String[] responseId = getResponseId(res);
                String resId = responseId[0];
                set.setStatus(3);
                set.setRespMessageId(resId.trim());
            } else {
                set.setStatus(-1);
                SmsCollect collectSet = new SmsCollect();
                SmsCollect collect = smsCollectMapper.selectOne(new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
                if (collect.getPendingNum() > 0) {
                    collectSet.setPendingNum(collect.getPendingNum() - 1);
                    if (collect.getPendingNum() == 1) {
                        collectSet.setStatus(SmsEnum.SUCCESS.getStatus());
                    }
                    collectSet.setFailNum(collect.getFailNum() + 1);
                }
                smsCollectMapper.update(collectSet, new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
            }
            smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", details.getDetailsId()));
        } catch (Exception e) {
            log.log(Level.WARNING, "post message error / response id get error", e);
        }
    }

    private String[] getResponseId(String res) {
        if (res.contains(":")) {
            return res.split(":")[1].split(",");
        } else if (res.contains("-")) {
            return res.split(",");
        }
        return null;
    }

    @Override
    public void run() {
        log.info("正在发送短信，号码: " + details.getPhone());
        try {
            this.submitMessage(details, account);
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "发送短信异常", e);
        }
    }
}
