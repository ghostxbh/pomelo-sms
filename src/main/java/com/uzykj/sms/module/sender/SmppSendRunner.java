package com.uzykj.sms.module.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.service.SmsDetailsService;
import com.uzykj.sms.module.sender.queue.SmsSendThreadPool;
import com.uzykj.sms.module.smpp.business.SmsSendBusiness;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author ghostxbh
 * @date 2020/8/25
 * @description
 */
@Slf4j
public class SmppSendRunner {
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private volatile static SmppSendRunner instance;
    private static int CORE = 10;
    private static int MAX = 10;
    private static int QUEUE = 1000;
    private final Thread mainThread;
    private final ThreadPoolExecutor executor;

    public SmppSendRunner() {
        executor = new ThreadPoolExecutor(CORE, MAX, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(QUEUE), new NamedThreadFactory("sms_send_pool"));

        mainThread = new Thread("SMPPSendQueueMainThread") {
            @Override
            public void run() {
                while (true) {
                    List<SmsDetails> sendList = getSendList();

                    log.info("SMPP send Thread task size: {} , active: {} , queue: {}",
                            sendList.size(), executor.getActiveCount(), executor.getQueue().size());

                    if (sendList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.error("thread sleep error {}", e);
                        }
                        continue;
                    }

                    for (SmsDetails details : sendList) {
                        log.info("main_send_task_{} ,active: {} ,queue: {} ,finish: {} ,taskId: {}",
                                details.getPhone(), executor.getActiveCount(), executor.getQueue().size(),
                                executor.getCompletedTaskCount(), details.getDetailsId());
                        // change vo status first
                        try {
                            while (executor.getQueue().size() >= QUEUE) {
                                synchronized (mainThread) {
                                    log.info("main_send_wait ,taskId: {}", details.getDetailsId());
                                    mainThread.wait();
                                }
                            }
                            changeStatus(details);
                            String code = getCode(details);

                            executor.execute(new EventTask(code, details));
                        } catch (Exception e) {
                            log.error("changeStatus error", e);
                        }
                        log.info("线程池中线程数目：" + executor.getPoolSize() + "，队列中等待执行的任务数目：" +
                                executor.getQueue().size() + "，已执行玩别的任务数目：" + executor.getCompletedTaskCount());

                    }
                }
            }
        };
    }

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

    public void shutdown() {
        try {
            executor.shutdown();
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("shutdown error", e);
        }
    }

    /**
     * 任务处理子线程
     */
    private class EventTask implements Runnable {
        String code;
        SmsDetails details;

        EventTask(String code, SmsDetails details) {
            this.code = code;
            this.details = details;
        }

        @Override
        public void run() {
            SmsSendBusiness business = new SmsSendBusiness(code, details);
            business.send();
            synchronized (mainThread) {
                log.info("do_notify_main,pool:" + executor.getPoolSize() + ",queue: " + executor.getQueue().size() + ",queueMaxNum:" + QUEUE);
                mainThread.notify();
            }
        }
    }


    /**
     * 使用命名线程工厂
     */
    static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (null == name || name.isEmpty()) {
                name = "pool";
            }
            // 拼接线程名
            namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
