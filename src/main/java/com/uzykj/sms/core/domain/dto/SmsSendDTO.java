package com.uzykj.sms.core.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
@Data
public class SmsSendDTO {
    /**
     * 运营商分配的短号
     */
    private String shortCode;
    /**
     * 短信目的号码列表
     */
    private List<String> mobiles;
    /**
     * 下发短信内容
     */
    private String content;

    private Integer userId;
}
