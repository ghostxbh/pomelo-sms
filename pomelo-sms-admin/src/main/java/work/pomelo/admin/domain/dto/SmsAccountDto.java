package work.pomelo.admin.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsAccountDto {
    private String code;
    private String systemId;
    private Integer enabled;
    private Integer isInvalid;
}
