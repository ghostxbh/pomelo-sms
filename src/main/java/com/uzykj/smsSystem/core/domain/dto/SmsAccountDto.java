package com.uzykj.smsSystem.core.domain.dto;

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
public class SmsAccountDto {
    private String code;
    private String systemId;
    private Integer enabled;
    private Integer isInvalid;
}
