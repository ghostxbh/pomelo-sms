package work.pomelo.admin.provider.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import work.pomelo.admin.common.ApplicationContextUtil;
import work.pomelo.admin.common.Globle;
import work.pomelo.admin.domain.SmsAccount;
import work.pomelo.admin.domain.SmsCollect;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.enums.SmsEnum;
import work.pomelo.admin.mapper.SmsCollectMapper;
import work.pomelo.admin.mapper.SmsDetailsMapper;
import work.pomelo.admin.provider.http.masterks.MasterksSender;
import work.pomelo.admin.provider.sender.queue.SmsSendThreadPool;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ghostxbh
 * @date 2020/7/3
 */
@Slf4j
public class HTTPSenderRunner {
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    private static final MasterksSender sender = new MasterksSender();
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
                            log.error("thread sleep error %s", e);
                        }
                        continue;
                    }

                    SmsAccount smsAccount = Globle.ACCOUNT_CACHE.get(collect.getAccountCode());
                    List<SmsDetails> detailsList = getDetailsList(collect.getCollectId());
                    Map<String, SmsDetails> detilsMap = getDetilsMap(detailsList);
                    List<String> sendList = getSendList(detailsList);

                    changeCollect(collect);
                    if (sendList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.error("thread sleep error %s", e);
                        }
                        continue;
                    }
                    log.info("[send runner] 获取到任务" + sendList.size() + "条");

                    // change vo status first
                    try {
                        sem.acquire();
                        changeStatus(detailsList);
                    } catch (Exception e) {
                        log.error("sem error", e);
                    }

                    SmsSendThreadPool.execute(() -> {
                        try {
                            sender.submitMessage(sendList, detilsMap, collect.getContents(), smsAccount);
                        } catch (Exception e) {
                            log.error("fatal process task error", e);
                        }
                        sem.release();
                    });
                }
            }
        };
        thread.start();
    }

    public SmsCollect getCollect() {
        return Optional
                .ofNullable(smsCollectMapper.getSendCollect())
                .orElse(null);
    }

    public List<SmsDetails> getDetailsList(String collectId) {
        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>()
                .eq("collect_id", collectId)
                .eq("status", 1);
        Page<SmsDetails> page = new Page<>(1, 200);
        Page<SmsDetails> selectPage = smsDetailsMapper.selectPage(page, query);
        List<SmsDetails> detailsList = selectPage.getRecords();

        return Optional.ofNullable(detailsList)
                .orElse(new ArrayList<SmsDetails>(0));
    }

    public List<String> getSendList(List<SmsDetails> detailsList) {
        return detailsList
                .stream()
                .map(SmsDetails::getPhone)
                .collect(Collectors.toList());
    }

    public Map<String, SmsDetails> getDetilsMap(List<SmsDetails> detailsList) {
        HashMap<String, SmsDetails> detailsHashMap = Maps.newHashMap();
        detailsList.forEach(smsDetails -> detailsHashMap.put(smsDetails.getPhone(), smsDetails));
        return detailsHashMap;
    }

    public void changeStatus(List<SmsDetails> detailsList) {
        List<Integer> ids = detailsList.stream()
                .map(SmsDetails::getId)
                .collect(Collectors.toList());
        smsDetailsMapper.batchSendStatus(ids);
    }

    public void changeCollect(SmsCollect collect) {
        if (collect.getStatus().equals(SmsEnum.SUBMITED.getStatus())) {
            SmsCollect setCollect = new SmsCollect();
            setCollect.setStatus(SmsEnum.PENDING.getStatus());
            setCollect.setUpdateTime(new Date());
            smsCollectMapper.update(setCollect, new QueryWrapper<SmsCollect>().eq("collect_id", collect.getCollectId()));
            return;
        }

        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>()
                .eq("collect_id", collect.getCollectId())
                .eq("status", 1);
        Integer count = smsDetailsMapper.selectCount(query);
        if (count > 0) {
            return;
        }

        SmsCollect setCollect = new SmsCollect();
        setCollect.setStatus(SmsEnum.SUCCESS.getStatus());
        setCollect.setUpdateTime(new Date());
        smsCollectMapper.update(setCollect, new QueryWrapper<SmsCollect>().eq("collect_id", collect.getCollectId()));
    }
}
