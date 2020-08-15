package com.uzykj.sms.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uzykj.sms.core.domain.SmsDetails;
import com.uzykj.sms.core.domain.dto.SmsDetailsDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
public interface SmsDetailsMapper extends BaseMapper<SmsDetails> {
    @Select("select count(1) from sms_details where user_id=#{userId} and DATE_FORMAT(create_time,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')")
    int currentCount(@Param("userId") int userId);

    List<SmsDetails> getList(SmsDetailsDto smsDetailsDto);

    int getListCount(SmsDetailsDto smsDetailsDto);
}
