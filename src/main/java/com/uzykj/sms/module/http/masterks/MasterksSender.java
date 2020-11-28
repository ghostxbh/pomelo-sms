package com.uzykj.sms.module.http.masterks;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.util.StringUtils;
import com.uzykj.sms.module.http.HttpSender;
import com.uzykj.sms.module.http.ebulk.EbulkSend;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.compress.utils.Lists;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author xbh
 * @date 2020/11/28
 * @description
 */
public class MasterksSender implements HttpSender {
    private static Logger log = Logger.getLogger(EbulkSend.class.getName());
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(16, 16, 60, TimeUnit.SECONDS, Queues.newLinkedBlockingQueue(1000));

    @Override
    public void submitMessage(List<String> phones, Map<String, SmsDetails> detilsMap, String message, SmsAccount account) {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("username", account.getSystemId());
        paramMap.put("password", account.getPassword());
        paramMap.put("type", "UNICODE");
        paramMap.put("sender", account.getCode());
        paramMap.put("message", message);

        List<List<String>> partition = ListUtils.partition(phones, 15);
        partition.forEach(phoneList -> {
            CompletableFuture.supplyAsync(() -> {
                paramMap.put("mobile", String.join(",", phoneList));
                try {
                    String post = HttpUtil.post(account.getUrl(), paramMap);
                    Map<String, String> response = getResponseId(post);
                    String collectId = detilsMap.get(phones.get(0)).getCollectId();

                    setFailList(phones, detilsMap, response);
                    setSuccessList(detilsMap, response);
                    updateCollect(phones, response, collectId);
                    log.info("batch send success, total: " + phoneList.size());
                } catch (Exception e) {
                    log.log(Level.WARNING, "post message error / response id get error", e);
                }
                return 1;
            }, EXECUTOR_SERVICE);
        });

        log.info("submitMessage success, total: " + phones.size());
    }

    private Map<String, String> getResponseId(String res) {
        Map<String, String> phoneMap = Maps.newHashMap();
        if (res.contains("|")) {
            String replaceAll = res.replaceAll("<br/>", "|");
            String[] split = replaceAll.split("\\|");

            List<String> phoneList = Lists.newArrayList();
            List<String> msgIdList = Lists.newArrayList();
            Arrays.asList(split)
                    .forEach(elment -> {
                        String trim = elment.trim();
                        if (StringUtils.isUUID(trim)) msgIdList.add(trim);
                        if (StringUtils.isInteger(trim)) phoneList.add(trim);
                    });
            for (int i = 0; i < phoneList.size(); i++) {
                phoneMap.put(phoneList.get(i), msgIdList.get(i));
            }
        }
        return phoneMap;
    }

    private void setSuccessList(Map<String, SmsDetails> detilsMap, Map<String, String> response){
        Set<String> keySet = response.keySet();
        keySet.forEach(key -> {
            String msgId = response.get(key);
            Long id = detilsMap.get(key).getId();
            SmsDetails smsDetails = SmsDetails.builder()
                    .status(3)
                    .respMessageId(msgId)
                    .build();
            log.info("send sms success info: " + smsDetails + " , id: " + id);
            smsDetailsMapper.update(smsDetails, new QueryWrapper<SmsDetails>().eq("id", id));
        });
    }

    private void setFailList(List<String> phones, Map<String, SmsDetails> detilsMap, Map<String, String> response){
        List<Long> ids = phones.stream()
                .filter(phone -> response.get(phone) == null)
                .map(phone -> detilsMap.get(phone).getId())
                .collect(Collectors.toList());

        if (ids.size() > 0) {
            log.info("send sms fail ids: " + ids.size());
            smsDetailsMapper.setFailList(ids);
        }
    }

    private void updateCollect(List<String> phones, Map<String, String> response, String collectId){
        int successNum = response.size();
        int totalNum = phones.size();
        int failNum = totalNum - successNum;
        SmsCollect collect = smsCollectMapper.selectOne(new QueryWrapper<SmsCollect>().eq("collect_id", collectId));

        String status = null;
        if (collect.getTotal() - (collect.getPendingNum() + totalNum) < 1)
            status = SmsEnum.SUCCESS.getStatus();
        SmsCollect build = SmsCollect.builder()
                .pendingNum(collect.getPendingNum() - failNum)
                .failNum(collect.getFailNum() + failNum)
                .status(status != null ? status : collect.getStatus())
                .build();
        log.info("updateCollect info: " + build.toString());

        smsCollectMapper.update(build, new QueryWrapper<SmsCollect>().eq("collect_id", collectId));
    }
}
