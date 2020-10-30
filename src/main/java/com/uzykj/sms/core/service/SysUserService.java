package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.mapper.SmsAccountMapper;
import com.uzykj.sms.core.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ghostxbh
 * @date 2020/8/14
 * @description
 */
@Service
public class SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SmsAccountMapper smsAccountMapper;

    public SysUser get(Integer id) {
        return sysUserMapper.selectById(id);
    }

    /**
     * 添加用户信息
     */
    public void add(SysUser user) {
        // 默认86
        if (user.getPhonePrefix() == null || "".equals(user.getPhonePrefix())) {
            user.setPhonePrefix("86");
        }
        sysUserMapper.insert(user);
        Globle.updateCache();
    }

    public void update(SysUser user) {
        sysUserMapper.updateById(user);
        Globle.updateCache();
    }

    /**
     * 删除用户信息
     */
    public void del(int id) {
        sysUserMapper.deleteById(id);
        Globle.updateCache();
    }

    public SysUser login(String name) {
        return sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("name", name));
    }

    /**
     * 根据id修改密码
     */
    public void resetPwd(String newPwd, SysUser user) {
        SysUser sysUser = new SysUser();
        sysUser.setPassword(newPwd);
        sysUserMapper.update(sysUser, new QueryWrapper<SysUser>().eq("id", user.getId()));
    }

    public void modifyAllowance(Integer userId, Integer allowance, Integer account,
                                String phonePrefix, Integer textSuffix) {
        SysUser sysUser = new SysUser();
        sysUser.setAllowance(allowance);
        sysUser.setAccountId(account);
        sysUser.setTextSuffix(textSuffix);
        if (phonePrefix != null) {
            sysUser.setPhonePrefix(phonePrefix);
        }
        sysUserMapper.update(sysUser, new QueryWrapper<SysUser>().eq("id", userId));
        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("id", userId));
        SmsAccount smsAccount = smsAccountMapper.selectOne(new QueryWrapper<SmsAccount>().eq("id", user.getAccountId()));
        user.setAccount(smsAccount);
        Globle.USER_CACHE.put(user.getId(), user);
    }

    /**
     * 查询所有用户
     */
    public Page<SysUser> getAllUser(PageDto pageDto, SysUser sysUser) {
        Page<SysUser> page = new Page<SysUser>(pageDto.getPage(), pageDto.getPageSize());
        QueryWrapper<SysUser> query = new QueryWrapper<SysUser>();
        if (sysUser.getName() != null) query.eq("name", sysUser.getName());
        if (sysUser.getMobile() != null) query.eq("mobile", sysUser.getMobile());
        page = sysUserMapper.selectPage(page, query);
        return page;
    }

    public int userAllCount(SysUser sysUser) {
        QueryWrapper<SysUser> query = new QueryWrapper<SysUser>();
        if (sysUser.getName() != null) query.eq("name", sysUser.getName());
        if (sysUser.getMobile() != null) query.eq("mobile", sysUser.getMobile());
        return sysUserMapper.selectCount(query);
    }

    public List<SysUser> allUser() {
        return sysUserMapper.getAll();
    }
}
