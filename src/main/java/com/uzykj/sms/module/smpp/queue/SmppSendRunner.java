package com.uzykj.sms.module.smpp.queue;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.service.SmsDetailsService;
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
    private static final SmsSendBusiness submit = new SmsSendBusiness();
    private volatile static SmppSendRunner instance;

    private static final int DEFAULT = 2000;

    private static final int LIMIT = 30;

    private ExecutorService exec;

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
        new Thread() {
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
                    exec = Executors.newFixedThreadPool(10);
                    try {
                        List<List<SmsDetails>> list = new ArrayList<>();
                        //1.定义CompletionService
                        CompletionService<List<SmsDetails>> completionService = new ExecutorCompletionService<>(exec);
                        List<Future<List<SmsDetails>>> futures = new ArrayList<Future<List<SmsDetails>>>();

                        // 每100个收件人创建一个线程用来发送
                        for (int i = 0; i <= sendList.size() / LIMIT; i++) {
                            List<SmsDetails> smsDetails = sendList.stream().skip(i * LIMIT).limit(LIMIT).collect(Collectors.toList());
                            changeStatus(smsDetails);
                            futures.add(completionService.submit(new CallableTask(smsDetails)));
                        }

                        for (int i = 0; i < sendList.size() / LIMIT; i++) {
                            List<SmsDetails> smsDetails = completionService.take().get();//采用completionService.take()，内部维护阻塞队列，任务先完成的先获取到
                            log.info("单列任务，序号：" + i + " 完成! 共发送： " + smsDetails.size() + " 条，时间：" + new Date());
                            list.add(smsDetails);
                        }
                        log.info("发送任务已完成，共 " + sendList.size() + " 条");
                        TimeUnit.SECONDS.sleep(2);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "send sms error", e);
                    } finally {
                        exec.shutdown();
                    }
                }
            }
        }.start();
    }

    public List<SmsDetails> getSendList() {
        List<SmsDetails> detailsList = smsDetailsMapper.selectList(new QueryWrapper<SmsDetails>().eq("status", 1));
        return Optional.ofNullable(detailsList)
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

    static class CallableTask implements Callable<List<SmsDetails>> {
        private List<SmsDetails> detailsList;

        public CallableTask(List<SmsDetails> detailsList) {
            super();
            this.detailsList = detailsList;
        }

        @Override
        public List<SmsDetails> call() throws Exception {
            for (SmsDetails details : detailsList) {
                String code = getCode(details);
                submit.send(code, details);
            }
            return detailsList;
        }

        public String getCode(SmsDetails smsDetails) {
            SysUser sysUser = Globle.USER_CACHE.get(smsDetails.getUserId());
            return Optional.ofNullable(sysUser.getAccount()
                    .getCode())
                    .orElse(null);
        }
    }
}
