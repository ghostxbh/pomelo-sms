package com.uzykj.sms.module.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.service.SmsDetailsService;
import com.uzykj.sms.module.http.ebulk.EbulkSender;
import com.uzykj.sms.module.sender.queue.SmsSendThreadPool;

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
public class HTTPSenderRunner {
    private static Logger log = Logger.getLogger(SmsDetailsService.class.getName());
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    private static final EbulkSender sender = new EbulkSender();
    private volatile static HTTPSenderRunner instance;
    private static final int DEFAULT = 100;
    private static Semaphore sem = new Semaphore(90);

    public static HTTPSenderRunner getInstance() {
        if (instance == null) {
            synchronized (HTTPSenderRunner.class) {
                if (instance == null) {
                    instance = new HTTPSenderRunner();
                }
            }
        }
        return instance;
    }

    public void start() {
        Thread thread = new Thread("http_send_main") {
            @Override
            public void run() {
                while (true) {
                    final SmsCollect collect = getCollect();
                    if (collect == null) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "thread sleep error {}", e);
                        }
                        continue;
                    }
                    final SmsAccount smsAccount = Globle.ACCOUNT_CACHE.get(collect.getAccountCode());
                    final Map<String, SmsDetails> detilsMap = getDetilsMap(collect.getCollectId());
                    final List<String> sendList = getSendList(collect.getCollectId());
                    if (sendList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "thread sleep error {}", e);
                        }
                        continue;
                    }
                    log.info("[send runner] 获取到任务" + sendList.size() + "条");

                    // change vo status first
                    try {
                        sem.acquire();
                        changeStatus(sendList, detilsMap, collect);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "sem error", e);
                    }
                    SmsSendThreadPool.execute(() -> {
                        try {
                            sender.submitMessage(sendList, detilsMap, collect.getContents(), smsAccount);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "fatal process task error", e);
                        }
                        sem.release();
                    });
                }
            }
        };
        thread.start();
    }

    public SmsCollect getCollect() {
        QueryWrapper<SmsCollect> query = new QueryWrapper<SmsCollect>()
                .eq("status", SmsEnum.SUBMITED.getStatus())
                .like("account_code", "H")
                .orderByAsc("create_time");
        return Optional
                .ofNullable(smsCollectMapper.selectOne(query))
                .orElse(null);
    }

    public List<String> getSendList(String collectId) {
        return Optional
                .ofNullable(smsDetailsMapper.sendPhoneList(collectId))
                .orElse(new ArrayList<String>(0));
    }

    public Map<String, SmsDetails> getDetilsMap(String collectId) {
        HashMap<String, SmsDetails> detailsHashMap = new HashMap<String, SmsDetails>();
        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>().eq("collect_id", collectId).eq("status", 1);
        Optional.ofNullable(smsDetailsMapper.selectList(query))
                .orElse(new ArrayList<SmsDetails>(0))
                .forEach(smsDetails -> detailsHashMap.put(smsDetails.getPhone(), smsDetails));
        return detailsHashMap;
    }

    public void changeStatus(List<String> phones, Map<String, SmsDetails> detailsHashMap, SmsCollect collect) {
        SmsCollect setCollect = new SmsCollect();
        setCollect.setStatus(SmsEnum.PENDING.getStatus());
        setCollect.setUpdateTime(new Date());
        smsCollectMapper.update(setCollect, new QueryWrapper<SmsCollect>().eq("collect_id", collect.getCollectId()));

        Optional.ofNullable(phones)
                .orElse(new ArrayList<String>(0))
                .forEach(phone -> {
                    SmsDetails set = new SmsDetails();
                    set.setStatus(2);
                    set.setSendTime(new Date());
                    smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", detailsHashMap.get(phone).getDetailsId()));
                });
    }
}
