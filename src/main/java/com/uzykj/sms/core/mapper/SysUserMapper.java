package com.uzykj.sms.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uzykj.sms.core.domain.SysUser;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<SysUser> getAll();
}
