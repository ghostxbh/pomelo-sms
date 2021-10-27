package work.pomelo.admin.common;

import lombok.extern.slf4j.Slf4j;
import work.pomelo.admin.domain.SmsAccount;
import work.pomelo.admin.domain.SysUser;
import work.pomelo.admin.mapper.SmsAccountMapper;
import work.pomelo.admin.mapper.SysUserMapper;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ghostxbh
 * @date 2020/7/5
 * 
 */
@Slf4j
public class Globle {
    private static final SysUserMapper sysUserMapper = ApplicationContextUtil.getApplicationContext().getBean(SysUserMapper.class);
    private static final SmsAccountMapper smsAccountMapper = ApplicationContextUtil.getApplicationContext().getBean(SmsAccountMapper.class);
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
