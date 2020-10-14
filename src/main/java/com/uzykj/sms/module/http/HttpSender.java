package com.uzykj.sms.module.http;

import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SmsDetails;

import java.util.List;
import java.util.Map;

/**
 * @author ghostxbh
 * @date 2020/7/9
 * @description 短信发送接口
 */
public interface HttpSender {
    /**
     * 短信发送
     *
     * @param phones
     * @param message
     * @param account
     */
    void submitMessage(List<String> phones, Map<String, SmsDetails> detilsMap, String message, SmsAccount account);
}
