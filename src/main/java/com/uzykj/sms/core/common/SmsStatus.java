package com.uzykj.sms.core.common;

import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author ghostxbh
 * @date 2020/8/15
 * @description
 */
public class SmsStatus {
    public static String switchSmsStatus(String status) {
        String result = "";
        if (!StringUtils.isEmpty(status)) {
            switch (status) {
                case "success":
                case "delivrd":
                    result = "发送完成";
                    break;
                case "expired":
                    result = "响应超时";
                    break;
                case "undeliv":
                    result = "无法送达";
                    break;
                case "rejectd":
                    result = "消息被拒绝";
                    break;
                case "deleted":
                    result = "消息被删除";
                    break;
                case "unknown":
                    result = "状态未知";
                    break;
                case "pending":
                case "submited":
                case "checking":
                default:
                    result = "已发送";
                    break;
            }
        }
        return result;
    }
    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = "https://e-bulksms.com/sendsms/sendsms?username=qq1562580&password=qq1314520&type=2&sid=855&message=你在干嘛呢？如果无聊~可以来这里噢！ https://is.gd/qZxy1Z 通知码&dcs=0&mno=8613752086588";
        String encode = URLEncoder.encode(s, "UTF-8");
        System.out.println(encode);
        String replaceAll = encode.replaceAll("\\+", "");
        System.out.println(replaceAll);
        String decode = URLDecoder.decode(replaceAll, "UTF-8");
        System.out.println(decode);
    }
}
