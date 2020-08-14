package com.uzykj.smsSystem.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sms_collect")
public class SmsCollect {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String collectId;
    private Integer userId;

    private String contents;
    private String status;

    private Integer total;
    private Integer pendingNum;
    private Integer successNum;
    private Integer failNum;

    private Date createTime;
    private Date updateTime;
    private Date completeTime;
}