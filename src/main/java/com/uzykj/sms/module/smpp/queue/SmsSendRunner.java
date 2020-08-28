package com.uzykj.sms.module.smpp.queue;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.service.SmsDetailsService;
import com.uzykj.sms.module.smpp.business.SmsSendBusiness;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ghostxbh
 * @date 2020/7/3
 * @description
 */
public class SmsSendRunner extends Globle {
    private static Logger log = Logger.getLogger(SmsDetailsService.class.getName());
    private static final SmsSendBusiness submit = new SmsSendBusiness();
    private volatile static SmsSendRunner instance;
    private static final int DEFAULT = 100;
    private static Semaphore sem = new Semaphore(90);

    public static SmsSendRunner getInstance() {
        if (instance == null) {
            synchronized (SmsSendRunner.class) {
                if (instance == null) {
                    instance = new SmsSendRunner();
                }
            }
        }
        return instance;
    }

    public void start() {
        Thread thread = new Thread("smpp_send_main") {
            @Override
            public void run() {
                while (true) {
                    final List<SmsDetails> sendList = getSendList();
                    if (sendList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "thread sleep error {}", e);
                        }
                        continue;
                    }
                    log.info("[send runner] 获取到任务" + sendList.size() + "条");
                    for (SmsDetails details : sendList) {
                        // change vo status first
                        try {
                            sem.acquire();
                            changeStatus(details);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "sem error", e);
                        }
                        SmsSendThreadPool.execute(() -> {
                            try {
                                String code = getCode(details);
                                submit.send(code, details);
                            } catch (Exception e) {
                                log.log(Level.WARNING, "fatal process task id: " + details.getId(), e);
                            }
                            sem.release();
                        });
                    }
                }
            }
        };
        thread.start();
    }

    public List<SmsDetails> getSendList() {
        return Optional
                .ofNullable(smsDetailsMapper.selectList(new QueryWrapper<SmsDetails>()
                        .eq("status", 1)))
                .orElse(new ArrayList<SmsDetails>(0));
    }

    public void changeStatus(SmsDetails smsDetails) {
        SmsDetails set = new SmsDetails();
        set.setStatus(2);
        set.setSendTime(new Date());
        smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", smsDetails.getDetailsId()));
    }

    public String getCode(SmsDetails smsDetails) {
        return Optional.ofNullable(Globle.USER_CACHE.get(smsDetails.getUserId()).getAccount()
                .getCode())
                .orElse(null);
    }
}
