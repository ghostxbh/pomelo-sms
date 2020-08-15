package com.uzykj.sms.core.domain.dto;

import java.util.List;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
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

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public List<String> getMobiles() {
        return mobiles;
    }

    public void setMobiles(List<String> mobiles) {
        this.mobiles = mobiles;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
