package com.uzykj.smsSystem.core.domain.dto;

import com.uzykj.smsSystem.core.domain.SmsAccount;
import com.uzykj.smsSystem.core.domain.SysUser;
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
