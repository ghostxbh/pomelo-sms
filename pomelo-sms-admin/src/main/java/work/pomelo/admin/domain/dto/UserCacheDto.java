package work.pomelo.admin.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import work.pomelo.admin.domain.SmsAccount;
import work.pomelo.admin.domain.SysUser;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCacheDto {
    private SysUser user;
    private SmsAccount account;
}
