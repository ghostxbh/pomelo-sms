package com.uzykj.smsSystem.core.common;

import com.uzykj.smsSystem.core.domain.SmsAccount;
import com.uzykj.smsSystem.core.domain.SysUser;
import com.uzykj.smsSystem.core.domain.dto.UserCacheDto;
import com.uzykj.smsSystem.core.mapper.SmsAccountMapper;
import com.uzykj.smsSystem.core.mapper.SmsCollectMapper;
import com.uzykj.smsSystem.core.mapper.SmsDetailsMapper;
import com.uzykj.smsSystem.core.mapper.SysUserMapper;
import com.uzykj.smsSystem.core.service.SysUserService;
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
@Component
public class Globle {
    public static SmsDetailsMapper smsDetailsMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsDetailsMapper.class);
    public static SmsAccountMapper smsAccountMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsAccountMapper.class);
    public static SmsCollectMapper smsCollectMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsCollectMapper.class);
    public static SysUserMapper sysUserMapper = ApplicationContextUtil.getApplicationContext().getBean(SysUserMapper.class);
    public static final ConcurrentHashMap<Integer, UserCacheDto> USER_CACHE = new ConcurrentHashMap<Integer, UserCacheDto>();

    @Autowired
    private SmsAccountMapper accountMapper;
    @Autowired
    private SysUserService sysUserService;

    @Bean
    public void allUserCache() {
        List<SysUser> users = sysUserService.allUser();
        List<SysUser> checkUsers = Optional.ofNullable(users).orElse(new ArrayList<SysUser>(0));
        checkUsers.forEach(user -> {
            SmsAccount smsAccount = accountMapper.selectById(user.getAccountId());
            UserCacheDto dto = new UserCacheDto(user, smsAccount);
            USER_CACHE.put(user.getId(), dto);
        });
    }
}
