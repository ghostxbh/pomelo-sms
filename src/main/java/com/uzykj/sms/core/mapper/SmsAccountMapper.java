package com.uzykj.sms.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SysUser;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
public interface SmsAccountMapper extends BaseMapper<SmsAccount> {
    @Select("SELECT * FROM sms_account")
    List<SmsAccount> getAll();
}
