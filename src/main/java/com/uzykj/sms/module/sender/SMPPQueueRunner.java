package com.uzykj.sms.module.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.module.smpp.business.SmsSendBusiness;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author ghostxbh
 * @date 2020/8/25
 * @description
 */
@Slf4j
public class SMPPQueueRunner {
    private SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private volatile static SMPPQueueRunner instance;
    private final Thread mainThread;

    public SMPPQueueRunner() {
        mainThread = new Thread("SMPPSendQueueMainThread") {
            @Override
            public void run() {
                while (true) {
                    List<SmsDetails> sendList = getSendList();

                    log.info("SMPP send Thread task size: {}", sendList.size());

                    if (sendList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.error("thread sleep error", e);
                        }
                        continue;
                    }
                    for (SmsDetails details : sendList) {
                        // change vo status first
                        try {
                            AsyncProcessQueue.execute(new Runnable() {
                                @Override
                                public void run() {
                                    changeStatus(details);
                                    String code = getCode(details);
                                    SmsSendBusiness business = new SmsSendBusiness(code, details);
                                    business.send();
                                }
                            });
                        } catch (Exception e) {
                            log.error("changeStatus error", e);
                        }
                    }
                }
            }
        };
    }

    public static SMPPQueueRunner getInstance() {
        if (instance == null) {
            synchronized (SMPPQueueRunner.class) {
                if (instance == null) {
                    instance = new SMPPQueueRunner();
                }
            }
        }
        return instance;
    }

    public List<SmsDetails> getSendList() {
        Page<SmsDetails> page = new Page<SmsDetails>(1, 500);
        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>()
                .eq("status", 1)
                .like("account_code", "S");

        Page<SmsDetails> detailsPage = smsDetailsMapper.selectPage(page, query);
        List<SmsDetails> detailsList = detailsPage.getRecords();

        return Optional
                .ofNullable(detailsList)
                .orElse(new ArrayList<SmsDetails>(0));
    }

    public void changeStatus(SmsDetails smsDetails) {
        SmsDetails set = new SmsDetails();
        set.setStatus(2);
        set.setSendTime(new Date());
        smsDetailsMapper.update(set,
                new QueryWrapper<SmsDetails>()
                        .eq("details_id", smsDetails.getDetailsId()));
    }

    public String getCode(SmsDetails smsDetails) {
        return Optional.ofNullable(Globle.USER_CACHE.get(smsDetails.getUserId()).getAccount()
                .getCode())
                .orElse(null);
    }

    public void start() {
        mainThread.start();
        mainThread.setName("SMPPSendQueueMainThread");
    }
}
