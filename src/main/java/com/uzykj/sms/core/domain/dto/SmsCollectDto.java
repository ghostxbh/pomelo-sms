package com.uzykj.sms.core.domain.dto;

import com.uzykj.sms.core.domain.SmsCollect;
import com.uzykj.sms.core.domain.SysUser;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SmsCollectDto extends SmsCollect {
    private SysUser user;

    public SmsCollectDto(SmsCollect collect, SysUser user) {
        super(collect);
        this.user = user;
    }
}