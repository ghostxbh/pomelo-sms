package com.uzykj.sms.module.http.ebulk;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.common.ApplicationContextUtil;
import com.uzykj.sms.core.common.http.HttpClient4;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.enums.SmsEnum;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.module.http.HttpSender;
import org.apache.commons.collections4.ListUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EbulkSender implements HttpSender {
    private static Logger log = Logger.getLogger(EbulkSender.class.getName());
    private static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    private static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    private static final int DEFAULT = 16;


    @Override
    public void submitMessage(List<String> phones, Map<String, SmsDetails> detilsMap, String message, SmsAccount account) {
        Map<String, Object> paramMap = new HashMap<String, Object>(6);
        paramMap.put("username", account.getSystemId());
        paramMap.put("password", account.getPassword());
        paramMap.put("type", "2");
        paramMap.put("sid", "855");
        paramMap.put("message", message);
        paramMap.put("dcs", "0");

        List<List<String>> partition = ListUtils.partition(phones, DEFAULT);
        Optional.of(partition)
                .orElse(new ArrayList<List<String>>(0))
                .forEach(sendList -> {
                    int size = sendList.size();
                    paramMap.put("mno", String.join(",", sendList));

                    try {
                        log.info("回调请求数据 url: " + account.getUrl() + ", 参数：" + paramMap);
                        String res = HttpClient4.doPost(account.getUrl(), paramMap);
                        log.info("回调数据打印：" + res);
                        boolean isSendSuccess = true;
                        if (res.contains("Message")) {
                            isSendSuccess = false;
                        }
                        for (int i = 0; i < size; i++) {
                            String phone = sendList.get(i);
                            SmsDetails details = detilsMap.get(phone);
                            SmsDetails set = new SmsDetails();
                            if (isSendSuccess) {
                                String[] responseID = getResponseID(res);
                                String resId = responseID[i];
                                set.setStatus(3);
                                set.setRespMessageId(resId.trim());
                            }else {
                                set.setStatus(-1);

                                SmsCollect collectSet = new SmsCollect();
                                SmsCollect collect = smsCollectMapper.selectOne(new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
                                if (collect.getPendingNum() > 0) {
                                    collectSet.setPendingNum(collect.getPendingNum() - size);
                                    if (collect.getPendingNum() == 1) {
                                        collectSet.setStatus(SmsEnum.SUCCESS.getStatus());
                                    }
                                }
                                collectSet.setFailNum(collect.getFailNum() + size);
                                smsCollectMapper.update(collectSet, new QueryWrapper<SmsCollect>().eq("collect_id", details.getCollectId()));
                            }
                            smsDetailsMapper.update(set, new QueryWrapper<SmsDetails>().eq("details_id", details.getDetailsId()));
                        }
                    } catch (Exception e) {
                        log.log(Level.WARNING, "post message error / response id get error", e);
                    }
                });
    }

    private String[] getResponseID(String res) {
        if (res.contains(":")) {
            return res.split(":")[1].split(",");
        } else if (res.contains("-")) {
            return res.split(",");
        }
        return null;
    }

    public static void main(String[] args) {
//        https://e-bulksms.com/sendsms/dlr?smscid=d4248487-9d91-454a-a138-63467ceded82
        String url = "https://e-bulksms.com/sendsms/sendsms";
        String account = "mie12366";
        String password = "qq1314520";
//        StringBuilder builder = new StringBuilder()
//                .append(url)
//                .append("?type=2")
//                .append("&dcs=0")
//                .append("&username=")
//                .append(account)
//                .append("&password=")
//                .append(password)
//                .append("&sid=")
//                .append("&message=")
//                .append("测试HTTP发送")
//                .append("&mno=");
        List<String> aa = new ArrayList<String>(2);
        aa.add("8619902463814");
        aa.add("8613763142417");
//        builder.append(String.join(",", aa));
//        System.out.println(new String(builder));
//        String doGet = HttpClient4.doGet(new String(builder));
//        System.out.println(doGet);

        Map<String, Object> paramMap = new HashMap<String, Object>(6);
        paramMap.put("username", account);
        paramMap.put("password", password);
        paramMap.put("type", "2");
        paramMap.put("mno", String.join(",", aa));
        paramMap.put("sid", "855");
        paramMap.put("message", "测试HTTP发送");
        paramMap.put("dcs", "0");

        String res = HttpClient4.doPost(url, paramMap);
        System.out.println(res);
//        String res = "Response : 47f156cb-9c3b-4ae2-a4d0-4af18749fc0d";
//        String[] split = res.split(":")[1].split(",");
//        Arrays.asList(split).forEach(s -> System.out.println(s.trim()));
    }
}
