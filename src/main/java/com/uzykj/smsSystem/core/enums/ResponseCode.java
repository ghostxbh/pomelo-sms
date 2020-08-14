package com.uzykj.smsSystem.core.enums;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
public enum ResponseCode {

    /**
     * 成功
     */
    OK(200, "OK", "OK"),
    BASE_ERR(50001, "exception", "基础异常"),
    PARAM_EMPTY(50002, "param empty", "参数为空"),
    PARAM_ERROR(50005, "error", "错误"),
    ;

    /**
     * 错误码
     */
    private int code;
    /**
     * 错误信息
     */
    private String msg;
    /**
     * 提示
     */
    private String tip;

    ResponseCode(int code, String msg, String tip) {
        this.code = code;
        this.msg = msg;
        this.tip = tip;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getTip() {
        return tip;
    }
}
