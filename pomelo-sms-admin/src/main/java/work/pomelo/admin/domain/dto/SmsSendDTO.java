package work.pomelo.admin.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
@Data
@Builder
public class SmsSendDTO {
    /**
     * 运营商分配的短号
     */
    private String shortCode;
    /**
     * 短信目的号码列表
     */
    private List<String> mobiles;
    /**
     * 下发短信内容
     */
    private String content;

    private Integer userId;
}
