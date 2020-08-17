package com.uzykj.sms.core.common;

import org.springframework.util.StringUtils;

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
                case "delivrd":
                    result = "发送成功";
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
}