package com.uzykj.sms.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ghostxbh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendPhoneDTO {
    List<String> phoneList;
    String content;
}
