package work.pomelo.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import work.pomelo.admin.domain.SmsCollect;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
public interface SmsCollectMapper extends BaseMapper<SmsCollect> {
    @Select("select IFNULL(sum(success_num),0) from sms_collect where user_id=#{userId} and DATE_FORMAT(create_time,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')")
    int successCountByDate(@Param("userId") int userId);

    @Select("select IFNULL(sum(fail_num),0) from sms_collect where user_id=#{userId} and DATE_FORMAT(create_time,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')")
    int failCountByDate(@Param("userId") int userId);

    @Select("SELECT * FROM sms_collect WHERE `status`='submited' OR `status`='pending' AND `account_code` LIKE CONCAT('%','H','%') ORDER BY create_time limit 1")
    SmsCollect getSendCollect();
}
