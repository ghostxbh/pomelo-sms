package com.uzykj.sms.core.common.json;

import com.uzykj.sms.core.enums.ResponseCode;

/**
 * api返回结果类
 * Create by xbh 2019-09-29
 */
public class JsonResult<T> {

    /* 成功 */
    private static final int SUCCESS = 200;
    /* 成功 */
    private static final int FAIL = 600;
    /* 错误 */
    private static final int ERROR = 500;

    /* 状态码 */
    private int code;

    /* 提示信息 */
    private String message;

    /* 业务数据 */
    private T data;


    public JsonResult() {
        this.code = SUCCESS;
        this.message = "OK";
    }

    public JsonResult(T data) {
        this();//调用当前类的构造方法
        this.data = data;
    }

    public JsonResult(Throwable e) {
        this.code = ERROR;
        this.message = e.getMessage();
    }

    //设置状态码和提示信息
    public JsonResult(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    //设置状态码和数据
    public JsonResult(int code, T data) {
        super();
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }


    public JsonResult<T> setCode(int code) {
        this.code = code;
        return this;
    }


    public String getMessage() {
        return message;
    }


    public JsonResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }


    public Object getData() {
        return data;
    }


    public JsonResult<T> setData(T data) {
        this.data = data;
        return this;
    }


    public static int getSuccess() {
        return SUCCESS;
    }

    public static int getFail() {
        return FAIL;
    }

    public static int getError() {
        return ERROR;
    }

    public static JsonResult toError(String message) {
        return new JsonResult(ERROR, message);
    }

    public static JsonResult error(ResponseCode responseCode) {
        return new JsonResult(responseCode.getCode(), responseCode.getTip());
    }
}
