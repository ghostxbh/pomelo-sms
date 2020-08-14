package com.uzykj.smsSystem.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uzykj.smsSystem.core.domain.SmsCollect;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
public interface SmsCollectMapper extends BaseMapper<SmsCollect> {
    @Select("select IFNULL(sum(success_num),0) from sms_collect where user_id=#{userId} and DATE_FORMAT(create_time,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')")
    int successCountByDate(@Param("userId") int userId);

    @Select("select IFNULL(sum(fail_num),0) from sms_collect where user_id=#{userId} and DATE_FORMAT(create_time,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')")
    int failCountByDate(@Param("userId") int userId);
}
