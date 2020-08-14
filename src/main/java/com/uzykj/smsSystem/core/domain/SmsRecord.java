package com.uzykj.smsSystem.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
@Data
@TableName("tb_sms")
@NoArgsConstructor
@AllArgsConstructor
public class SmsRecord {

    private Long id;
    /**
     * 短信唯一标识
     */
    private String messageId;
    /**
     * SUBMIT_SM_RESP message_id, 由 SMSC 产生, 用于以后查询及替换短消息用, 或是表明状态报告所对应的源消息
     */
    private String respMessageId;
    /**
     * 短信方向, 1: 上行, 2: 下行
     */
    private Integer direction;
    /**
     * 对端号码
     */
    private String oppositeCode;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 短信内容
     */
    private String content;
    /**
     * 短信发送批次号
     */
    private String batchId;
    /**
     * 短信发送时间
     */
    private Date sendTime;
    /**
     * 接收短信时间
     */
    private Date receiveTime;
    /**
     * 短信发送状态, 0: 发送中、1: 已发送、-1: 发送失败
     */
    private Integer sendStatus;
    /**
     * 状态报告
     */
    private String reportStat;
    /**
     * 备注信息
     */
    private String remark;
    private Date createTime;
    private Date updateTime;
}
