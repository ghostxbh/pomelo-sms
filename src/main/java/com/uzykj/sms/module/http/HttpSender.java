package com.uzykj.sms.module.http;

import com.uzykj.sms.module.http.domian.HttpSendDTO;

/**
 * @author ghostxbh
 * @date 2020/7/9
 * @description 短信发送接口
 */
public interface HttpSender {
    /**
     * 短信发送
     *
     * @param sender
     * @return
     */
    void submitMessage(HttpSendDTO sender);
}
