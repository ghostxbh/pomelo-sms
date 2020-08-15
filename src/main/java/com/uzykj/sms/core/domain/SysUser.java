package com.uzykj.sms.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
@Data
@TableName("sys_user")
@AllArgsConstructor
@NoArgsConstructor
public class SysUser implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer accountId;
    private String name;
    private String password;
    private Integer allowance;
    private Double rate;
    private Integer sendLimit;
    private Integer conLimit;
    private String contactor;
    private String mobile;
    private String industry;
    private String company;
    private String address;
    private String role;
    private String remark;
    private Date createTime;

    public SysUser(SysUser user) {
        this.id = user.getId();
        this.accountId = user.getAccountId();
        this.name = user.getName();
        this.password = user.getPassword();
        this.allowance = user.getAllowance();
        this.rate = user.getRate();
        this.sendLimit = user.getSendLimit();
        this.conLimit = user.getConLimit();
        this.contactor = user.getContactor();
        this.mobile = user.getMobile();
        this.industry = user.getIndustry();
        this.company = user.getCompany();
        this.address = user.getAddress();
        this.role = user.getRole();
        this.remark = user.getRemark();
        this.createTime = user.getCreateTime();
    }
}
