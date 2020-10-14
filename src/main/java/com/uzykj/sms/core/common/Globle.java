package com.uzykj.sms.core.common;

import com.uzykj.sms.core.domain.SmsAccount;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ghostxbh
 * @date 2020/7/5
 * @description
 */
public class Globle {
    private static Logger log = LogManager.getLogger(Globle.class);
    private static SysUserMapper sysUserMapper = ApplicationContextUtil.getApplicationContext().getBean(SysUserMapper.class);
    private static SmsAccountMapper smsAccountMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsAccountMapper.class);
    public static final ConcurrentHashMap<Integer, SysUser> USER_CACHE = new ConcurrentHashMap<Integer, SysUser>();
    public static final ConcurrentHashMap<String, SmsAccount> ACCOUNT_CACHE = new ConcurrentHashMap<String, SmsAccount>();

    public static void initCache() {
        log.info("初始化用户列表缓存");
        Optional.ofNullable(sysUserMapper.getAll())
                .orElse(new ArrayList<SysUser>(0))
                .forEach(user -> USER_CACHE.put(user.getId(), user));

        log.info("初始化账户列表缓存");
        Optional.ofNullable(smsAccountMapper.getAll())
                .orElse(new ArrayList<SmsAccount>(0))
                .forEach(smsAccount -> ACCOUNT_CACHE.put(smsAccount.getCode(), smsAccount));
    }

    public static void updateCache() {
        log.info("同步用户列表缓存");
        Optional.ofNullable(sysUserMapper.getAll())
                .orElse(new ArrayList<SysUser>(0))
                .forEach(user -> USER_CACHE.put(user.getId(), user));

        log.info("同步账户列表缓存");
        Optional.ofNullable(smsAccountMapper.getAll())
                .orElse(new ArrayList<SmsAccount>(0))
                .forEach(smsAccount -> ACCOUNT_CACHE.put(smsAccount.getCode(), smsAccount));
    }


}
