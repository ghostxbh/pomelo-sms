package com.uzykj.sms.module.http.ebulk;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.http.HttpClient4;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;

import java.util.Date;
import java.util.logging.Logger;

/**
 * @author ghostxbh
 * @date 2020/10/13
 * @description
 */
public class EBulkCallback {
    private static Logger log = Logger.getLogger(EBulkCallback.class.getName());
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);

    public void reqMsgId(SmsDetails details) {
        String url = "https://e-bulksms.com/sendsms/dlr?smscid=" + details.getRespMessageId();
        String resStatus = HttpClient4.doGet(url);
        String status = getStatus(resStatus);
        excute(status, details);
    }

    private String getStatus(String resStatus) {
        if (resStatus.contains(":")) {
            return resStatus.split(":")[1].trim();
        }
        return null;
    }

    public void excute(String status, SmsDetails details) {
        log.info("回调成功，msgId: " + details.getRespMessageId() + " , status: " + status);

        SmsDetails set = new SmsDetails();
        if ("DELIVRD".equalsIgnoreCase(status)) {
            set.setStatus(10);
        } else {
            set.setStatus(-1);
        }
        set.setReceiveTime(new Date());
        set.setReportStat(status);
        smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", details.getDetailsId()));

        SmsCollect collect = smsCollectMapper.selectOne(new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
        SmsCollect collectSet = new SmsCollect();
        if (collect.getPendingNum() > 0) {
            collectSet.setPendingNum(collect.getPendingNum() - 1);
            if (collect.getPendingNum() == 1) {
                collectSet.setStatus(SmsEnum.SUCCESS.getStatus());
            }
        }
        if (status.equalsIgnoreCase(SmsEnum.DELIVRD.getStatus())) {
            collectSet.setSuccessNum(collect.getSuccessNum() + 1);
        } else {
            collectSet.setFailNum(collect.getFailNum() + 1);
        }
        smsCollectMapper.update(collectSet, new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
    }
}
