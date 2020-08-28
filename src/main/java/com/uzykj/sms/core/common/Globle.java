package com.uzykj.sms.core.common;

import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.mapper.SmsAccountMapper;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.mapper.SysUserMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ghostxbh
 * @date 2020/7/5
 * @description
 */
@Component
@Order(2)
public class Globle {
    private static Logger log = LogManager.getLogger(Globle.class);
    public static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    public static SmsAccountMapper smsAccountMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsAccountMapper.class);
    public static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    public static SysUserMapper sysUserMapper = ApplicationContextUtil.getApplicationContext().getBean(SysUserMapper.class);
    public static final ConcurrentHashMap<Integer, SysUser> USER_CACHE = new ConcurrentHashMap<Integer, SysUser>();

    @Bean
    public void initCache() {
        log.info("初始化用户列表缓存");
        Optional.ofNullable(sysUserMapper.getAll())
                .orElse(new ArrayList<SysUser>(0))
                .forEach(user -> USER_CACHE.put(user.getId(), user));
    }
}
