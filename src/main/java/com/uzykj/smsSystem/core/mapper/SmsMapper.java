package com.uzykj.smsSystem.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uzykj.smsSystem.core.domain.SmsRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author elmer.shao
 * @since 2020-08-08
 */
public interface SmsMapper extends BaseMapper<SmsRecord> {

    @Insert("<script>" +
            "INSERT INTO tb_sms(message_id, direction, opposite_code, mobile, content, batch_id," +
            "                   send_status, send_time, receive_time, remark)" +
            "  VALUES " +
            "  <foreach collection='smsList' item='sms' open='' close='' separator=','>" +
            "    (#{sms.messageId}, #{sms.direction}, #{sms.oppositeCode}, #{sms.mobile}, #{sms.content}," +
            "            #{sms.batchId}, #{sms.sendStatus}, #{sms.sendTime}, #{sms.receiveTime}, #{sms.remark})" +
            "  </foreach>" +
            "</script>")
    int batchSaveSmsRecords(@Param("smsList") List<SmsRecord> smsList);
}
