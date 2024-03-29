package work.pomelo.admin.provider.sender;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import work.pomelo.admin.common.ApplicationContextUtil;
import work.pomelo.admin.common.Globle;
import work.pomelo.admin.domain.SmsAccount;
import work.pomelo.admin.domain.SmsCollect;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.enums.SmsEnum;
import work.pomelo.admin.mapper.SmsCollectMapper;
import work.pomelo.admin.mapper.SmsDetailsMapper;
import work.pomelo.admin.provider.http.ebulk.EbulkSend;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ghostxbh
 * @date 2020/7/3
 */
@Slf4j
public class HttpSendRunner {
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    private volatile static HttpSendRunner instance;
    private Thread processor;
    private static int CORE = 10;
    private static int MAX = 10;
    ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE, MAX, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(20000));

    public static HttpSendRunner getInstance() {
        if (instance == null) {
            synchronized (HttpSendRunner.class) {
                if (instance == null) {
                    instance = new HttpSendRunner();
                }
            }
        }
        return instance;
    }

    public void start() {
        processor = new Thread("http_send_main") {
            @Override
            public void run() {
                while (true) {
                    final SmsCollect collect = getCollect();
                    if (collect == null) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                            log.error("thread sleep error: ", e);
                        }
                        continue;
                    }
                    final SmsAccount smsAccount = Globle.ACCOUNT_CACHE.get(collect.getAccountCode());
                    final List<SmsDetails> detailsList = getDetails(collect.getCollectId());

                    if ((detailsList == null || detailsList.size() == 0) && collect != null) {
                        try {
                            changeCollect(collect);
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.error("thread sleep error: ", e);
                        }
                        continue;
                    }

                    if (detailsList.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            log.error("thread sleep error: ", e);
                        }
                        continue;
                    }
                    log.info("[send runner] 获取到任务" + detailsList.size() + "条");

                    for (SmsDetails details : detailsList) {
                        // change vo status first
                        try {
                            changeStatus(details);
                        } catch (Exception e) {
                            log.error("changeStatus error", e);
                        }
                        EbulkSend business = new EbulkSend(details, smsAccount);
                        executor.execute(business);
                        log.info("线程池中线程数目：" + executor.getPoolSize() + "，队列中等待执行的任务数目：" +
                                executor.getQueue().size() + "，已执行玩别的任务数目：" + executor.getCompletedTaskCount());

                    }
                }
            }
        };
        processor.start();
    }

    public SmsCollect getCollect() {
        Page<SmsCollect> page = new Page<SmsCollect>(0, 1);
        QueryWrapper<SmsCollect> query = new QueryWrapper<SmsCollect>()
                .eq("status", SmsEnum.SUBMITED.getStatus())
                .like("account_code", "H")
                .orderByAsc("create_time");
        Page<SmsCollect> collectPage = smsCollectMapper.selectPage(page, query);
        List<SmsCollect> records = collectPage.getRecords();
        return Optional
                .ofNullable(records != null && records.size() > 0 ? records.get(0) : null)
                .orElse(null);
    }

    public List<SmsDetails> getDetails(String collectId) {
        HashMap<String, SmsDetails> detailsHashMap = new HashMap<String, SmsDetails>();
        QueryWrapper<SmsDetails> query = new QueryWrapper<SmsDetails>().eq("collect_id", collectId).eq("status", 1);
        return new ArrayList<>(Optional.ofNullable(smsDetailsMapper.selectList(query))
                .orElse(new ArrayList<SmsDetails>(0)));
    }


    public void changeCollect(SmsCollect collect) {
        SmsCollect setCollect = new SmsCollect();
        setCollect.setStatus(SmsEnum.SUCCESS.getStatus());
        setCollect.setUpdateTime(new Date());
        smsCollectMapper.update(setCollect, new QueryWrapper<SmsCollect>().eq("collect_id", collect.getCollectId()));
    }

    public void changeStatus(SmsDetails smsDetails) {
        SmsDetails set = new SmsDetails();
        set.setStatus(2);
        set.setSendTime(new Date());
        smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", smsDetails.getDetailsId()));
    }

    public void shutdown() {
        try {
            executor.shutdown();
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            log.error("shutdown error", e);
        }
    }
}
