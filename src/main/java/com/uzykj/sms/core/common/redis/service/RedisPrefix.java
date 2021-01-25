package com.uzykj.sms.core.common.redis.service;

/**
 * @author xbh
 * @date 2021/1/22
 * @description
 */
public interface RedisPrefix {
    /**
     * 汇总
     */
    String COLLECT = "sms:collect:";
    /**
     * 详情
     */
    String DETAIL = "sms:detail:";
    /**
     * 批次
     */
    String BATCH = "sms:batch:";
    /**
     * 响应
     */
    String RESP = "sms:resp:";
}
