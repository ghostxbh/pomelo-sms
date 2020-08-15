package com.uzykj.sms.core.domain.dto;

import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SysUser;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
@Data
@NoArgsConstructor
public class SysUserDto extends SysUser {
    private SmsAccount account;

    public SysUserDto(SysUser user, SmsAccount account) {
        super(user);
        this.account = account;
    }
}
