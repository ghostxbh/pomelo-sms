package com.uzykj.sms.core.enums;

import lombok.Getter;

@Getter
public enum ChannelEnum {
    SUCCESS(200, "线路通道链接成功"), FAIL(500, "线路通道链接失败")
    ;
    /**
     * 编码
     */
    private Integer code;
    /**
     * 信息
     */
    private String message;

    ChannelEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
