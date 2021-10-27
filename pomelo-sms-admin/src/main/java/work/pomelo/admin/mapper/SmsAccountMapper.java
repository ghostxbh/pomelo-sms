package work.pomelo.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import work.pomelo.admin.domain.SmsAccount;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
public interface SmsAccountMapper extends BaseMapper<SmsAccount> {
    @Select("SELECT * FROM sms_account")
    List<SmsAccount> getAll();
}
