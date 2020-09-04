package com.uzykj.sms.core.common;

import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.mapper.SmsAccountMapper;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.core.mapper.SysUserMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
public class Globle {
    private static Logger log = LogManager.getLogger(Globle.class);
    public static final ConcurrentHashMap<Integer, SysUser> USER_CACHE = new ConcurrentHashMap<Integer, SysUser>();

    @Autowired
    private SysUserMapper sysUserMapper;

    @Bean
    public void initCache() {
        log.info("初始化用户列表缓存");
        Optional.ofNullable(sysUserMapper.getAll())
                .orElse(new ArrayList<SysUser>(0))
                .forEach(user -> USER_CACHE.put(user.getId(), user));
    }
}
