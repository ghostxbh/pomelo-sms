package com.uzykj.smsSystem.core.enums;

import lombok.Getter;

@Getter
public enum SmsEnum {
    /* 短信发送状态 */
    NOMUST(400, "必填项未输入"), ALLOWANCE(401, "余额不足"),
    MAXOUT(402, "超过短信发送上限"),
    /* 短信异常状态 */
    INVALIDURL(1001, "无效网址"), INVALIDACCOUNT(1002, "无效账户或密码"),
    INVALIDTYPE(1003, "无效短信类型"), INVALIDMESSAGE(1004, "无效短信"),
    INVALIDMOBILE(1005, "无效号码"), INVALIDSENDER(1006, "无效发件人"),
    INSUFFICIENTCREDIT(1007, "余额不足"), INTERNALERROR(1008, "内部错误"),
    SERVERAVAILABLE(1009, "短信服务异常"),
    /* 短信回执 */
    DELIVRD("delivrd"), EXPIRED("expired"), UNDELIV("undeliv"),
    REJECTD("rejectd"),DELETED("deleted"),UNKNOWN("unknown"),
    /* 短信状态 */
    SUBMITED("submited"),PENDING("pending"),CHECKING("checking"),CALLBACK("callback"),
    SUCCESS("success"),FAIL("fail");

    /**
     * 编码
     */
    private Integer code;
    /**
     * 信息
     */
    private String message;
    /**
     * 状态
     */
    private String status;

    SmsEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    SmsEnum(String status) {
        this.status = status;
    }
}
