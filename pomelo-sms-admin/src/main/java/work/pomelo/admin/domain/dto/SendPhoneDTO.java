package work.pomelo.admin.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendPhoneDTO {
    List<String> phoneList;
    String content;
}
