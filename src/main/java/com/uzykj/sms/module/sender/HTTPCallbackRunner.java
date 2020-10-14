package com.uzykj.sms.module.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.service.SmsDetailsService;
import com.uzykj.sms.module.http.ebulk.EBulkCallback;
import com.uzykj.sms.module.sender.queue.SmsSendThreadPool;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ghostxbh
 * @date 2020/7/3
 * @description
 */
public class HTTPCallbackRunner {
    private static Logger log = Logger.getLogger(HTTPCallbackRunner.class.getName());
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static final EBulkCallback callback = new EBulkCallback();
    private volatile static HTTPCallbackRunner instance;
    private static Semaphore sem = new Semaphore(90);

    public static HTTPCallbackRunner getInstance() {
        if (instance == null) {
            synchronized (HTTPCallbackRunner.class) {
                if (instance == null) {
                    instance = new HTTPCallbackRunner();
                }
            }
        }
        return instance;
    }

    public void start() {
        Thread thread = new Thread("http_callback_main") {
            @Override
            public void run() {
                while (true) {
                    final List<SmsDetails> callbackList = getCallbackList();
                    if (callbackList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "thread sleep error {}", e);
                        }
                        continue;
                    }
                    log.info("[send runner] 获取到任务" + callbackList.size() + "条");
                    for (SmsDetails details : callbackList) {
                        SmsSendThreadPool.execute(() -> {
                            try {
                                callback.reqMsgId(details);
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

    public List<SmsDetails> getCallbackList() {
        Page<SmsDetails> page = new Page<SmsDetails>(0, 1000);
        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>().eq("status", 3).isNotNull(true, "resp_message_id").orderByAsc("create_time");
        Page<SmsDetails> selectPage = smsDetailsMapper.selectPage(page, query);
        return Optional.ofNullable(selectPage.getRecords())
                .orElse(new ArrayList<SmsDetails>(0));
    }
}
