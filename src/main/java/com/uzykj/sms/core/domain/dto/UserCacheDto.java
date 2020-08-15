package com.uzykj.sms.core.domain.dto;

import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCacheDto {
    private SysUser user;
    private SmsAccount account;
}
