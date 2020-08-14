package com.uzykj.smsSystem.core.enums;

import lombok.Getter;

@Getter
public enum UserEnum {
    NOMUST(400, "必填项未输入"), PWDERROR(401, "密码验证失败"),
    NOEXIST(402, "不存在"), EXIST(403, "已存在"),
    OLDERROR(404, "原密码错误"), NOMODIFY(405, "无修改")
    ;
    /**
     * 编码
     */
    private Integer code;
    /**
     * 信息
     */
    private String message;

    UserEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
