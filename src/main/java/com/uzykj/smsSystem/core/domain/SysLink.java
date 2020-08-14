package com.uzykj.smsSystem.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ghostxbh
 * @date 2020/8/11
 * @description
 */
@Data
@TableName("sys_link")
public class SysLink implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String url;
    private String enable;
    private Integer creator;
    private String remark;
    private Date createTime;
}
