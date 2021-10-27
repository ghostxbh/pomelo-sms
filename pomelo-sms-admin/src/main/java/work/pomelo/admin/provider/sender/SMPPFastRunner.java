package work.pomelo.admin.provider.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import work.pomelo.admin.common.ApplicationContextUtil;
import work.pomelo.admin.common.Globle;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.mapper.SmsDetailsMapper;
import work.pomelo.admin.provider.smpp.business.SmsSendBusiness;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ghostxbh
 * @date 2020/8/25
 * 
 */
@Slf4j
public class SMPPFastRunner {
    private SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private volatile static SMPPFastRunner instance;
    private static final int CORE = 8;
    private static final int MAX = 8;
    private static final int QUEUE = 5000;
    private final Thread mainThread;
    private final ThreadPoolExecutor executor;

    public SMPPFastRunner() {
        executor = new ThreadPoolExecutor(CORE, MAX, 60, TimeUnit.SECONDS,
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
                            log.error("thread sleep error", e);
                        }
                        continue;
                    }

                    for (SmsDetails details : sendList) {

                        // change vo status first
                        try {
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

    public static SMPPFastRunner getInstance() {
        if (instance == null) {
            synchronized (SMPPFastRunner.class) {
                if (instance == null) {
                    instance = new SMPPFastRunner();
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
