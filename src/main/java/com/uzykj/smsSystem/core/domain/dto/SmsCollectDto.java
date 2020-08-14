package com.uzykj.smsSystem.core.domain.dto;

import com.uzykj.smsSystem.core.domain.SmsCollect;
import com.uzykj.smsSystem.core.domain.SysUser;
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