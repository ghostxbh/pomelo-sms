package work.pomelo.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.domain.dto.SmsDetailsDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * 
 */
public interface SmsDetailsMapper extends BaseMapper<SmsDetails> {
    @Select("select count(1) from sms_details where user_id=#{userId} and DATE_FORMAT(create_time,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')")
    int currentCount(@Param("userId") int userId);

    List<SmsDetails> getList(SmsDetailsDto smsDetailsDto);

    int getListCount(SmsDetailsDto smsDetailsDto);

    List<String> getPhoneList(SmsDetailsDto smsDetailsDto);

    int setFailList(@Param("ids") List<Integer> ids);

    int batchSendStatus(@Param("ids") List<Integer> ids);

    @Select("select phone from sms_details where collect_id=#{collectId}")
    List<String> sendPhoneList(@Param("collectId")String collectId);
}
