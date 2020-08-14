package com.uzykj.smsSystem.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("sms_collect")
@NoArgsConstructor
@AllArgsConstructor
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

    public SmsCollect(SmsCollect collect) {
        this.id = collect.getId();
        this.collectId = collect.getCollectId();
        this.userId = collect.getUserId();
        this.contents = collect.getContents();
        this.status = collect.getStatus();
        this.total = collect.getTotal();
        this.pendingNum = collect.getPendingNum();
        this.successNum = collect.getSuccessNum();
        this.failNum = collect.getFailNum();
        this.createTime = collect.getCreateTime();
        this.updateTime = collect.getUpdateTime();
        this.completeTime = collect.getCompleteTime();
    }
}