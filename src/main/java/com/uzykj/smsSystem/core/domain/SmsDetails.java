package com.uzykj.smsSystem.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
@Data
@TableName("sms_details")
public class SmsDetails {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 短信唯一标识
     */
    private String detailsId;
    /**
     * SUBMIT_SM_RESP message_id, 由 SMSC 产生, 用于以后查询及替换短消息用, 或是表明状态报告所对应的源消息
     */
    private String respMessageId;
    /**
     * 汇总id
     */
    private String collectId;

    private Integer userId;
    /**
     * 短信方向, 1: 上行, 2: 下行
     */
    private Integer direction;
    /**
     * 对端号码
     */
    private String accountCode;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 短信内容
     */
    private String contents;
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
     * 短信发送状态, -1 发送失败 10、发送成功 1、已提交 2、已发送 3、其他
     */
    private Integer status;
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
