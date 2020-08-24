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
    private static final int DEFAULT = 1000;
    private static Semaphore sem = new Semaphore(10);

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
        Thread thread = new Thread("smpp_send_" + Thread.currentThread().getName()) {
            @Override
            public void run() {
                while (true) {
                    final List<SmsDetails> sendList = getSendList();
                    if (sendList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "thread sleep error", e);
                        }
                        continue;
                    }

                    try {
                        sem.acquire();
                        changeStatus(sendList);
                    } catch (InterruptedException e) {
                        log.log(Level.WARNING, "sem acquire error", e);
                    }

                    long millis = System.currentTimeMillis();
                    SmsSendThreadPool.execute(() -> {
                        try {
                            log.info("[task runner] send sms: " + Thread.currentThread().getName());
                            String code = getCode(sendList.get(0));
                            submit.batchSend(code, sendList);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "fatal process task ", e);
                        }
                        sem.release();
                    });
                    log.info("此500短信发送时间： " + (System.currentTimeMillis() - millis) + "s");
                }
            }
        };
        thread.start();
    }

    public SmsCollect orderCollect() {
        QueryWrapper<SmsCollect> query = new QueryWrapper<SmsCollect>();
        query.orderByDesc("create_time");
        return smsCollectMapper.selectOne(query);
    }

    public List<SmsDetails> getSendList() {
        SmsCollect collect = orderCollect();
        Page<SmsDetails> page = new Page<SmsDetails>(0, DEFAULT);
        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>()
                .eq("status", 1)
                .eq("collect_id", collect.getId());

        Page<SmsDetails> selectPage = smsDetailsMapper.selectPage(page, query);
        return Optional
                .ofNullable(selectPage.getRecords())
                .orElse(new ArrayList<SmsDetails>(0));
    }

    public void changeStatus(SmsDetails smsDetails) {
        SmsDetails set = new SmsDetails();
        set.setStatus(2);
        set.setSendTime(new Date());
        smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", smsDetails.getDetailsId()));
    }

    public void changeStatus(List<SmsDetails> smsDetails) {
        smsDetails.forEach(detail -> {
            SmsDetails set = new SmsDetails();
            set.setStatus(2);
            smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", detail.getDetailsId()));
        });
    }

    public String getCode(SmsDetails smsDetails) {
        SysUser sysUser = Globle.USER_CACHE.get(smsDetails.getUserId());
        return Optional.ofNullable(sysUser.getAccount()
                .getCode())
                .orElse(null);
    }
}
