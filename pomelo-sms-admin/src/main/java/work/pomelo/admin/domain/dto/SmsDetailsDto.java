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
@NoArgsConstructor
@AllArgsConstructor
public class SmsDetailsDto {
    private Integer userId;
    private String collectId;
    private String searchName;
    private String searchPhone;
    private String startTime;
    private String endTime;
    private PageDto pageDto;
}
