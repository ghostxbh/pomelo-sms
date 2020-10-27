package com.uzykj.sms.module.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.service.SmsDetailsService;
import com.uzykj.sms.module.sender.queue.SmsSendThreadPool;
import com.uzykj.sms.module.smpp.business.SmsSendBusiness;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author ghostxbh
 * @date 2020/8/25
 * @description
 */
public class SmppSendRunner {
    private static Logger log = Logger.getLogger(SmsDetailsService.class.getName());
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private volatile static SmppSendRunner instance;
    private Thread processor;
    private static int CORE = 16;
    private static int MAX = 16;
    ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE, MAX, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(20000));

    public static SmppSendRunner getInstance() {
        if (instance == null) {
            synchronized (SmppSendRunner.class) {
                if (instance == null) {
                    instance = new SmppSendRunner();
                }
            }
        }
        return instance;
    }

    public void start() {
        processor = new Thread("smpp_send_main") {
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
                            changeStatus(details);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "changeStatus error", e);
                        }
                        String code = getCode(details);
                        SmsSendBusiness business = new SmsSendBusiness(code, details);
                        executor.execute(business);
                        log.info("线程池中线程数目：" + executor.getPoolSize() + "，队列中等待执行的任务数目：" +
                                executor.getQueue().size() + "，已执行玩别的任务数目：" + executor.getCompletedTaskCount());

                    }
                }
            }
        };
        processor.start();
    }

    public List<SmsDetails> getSendList() {
        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>()
                .eq("status", 1)
                .like("account_code", "S");
        return Optional
                .ofNullable(smsDetailsMapper.selectList(query))
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

    public void shutdown() {
        try {
            executor.shutdown();
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.log(Level.WARNING, "shutdown error", e);
        }
    }
}
