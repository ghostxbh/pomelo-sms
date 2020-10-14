package com.uzykj.sms.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.uzykj.sms.core.enums.ChannelTypeEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
@Data
@TableName("sms_account")
public class SmsAccount {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private ChannelTypeEnum channelType;
    private String systemId;
    private String password;
    private String url;
    private Integer port;
    private String channelPwd;
    private String description;
    private Integer enabled;
    private Integer isInvalid;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
