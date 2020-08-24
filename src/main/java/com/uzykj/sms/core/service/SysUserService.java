package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SysUser;
import com.uzykj.sms.core.domain.dto.PageDto;
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

    public SysUser get(Integer id) {
        return sysUserMapper.selectById(id);
    }

    /**
     * 添加用户信息
     */
    public void add(SysUser user) {
        sysUserMapper.insert(user);
        SysUser newUser = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("name", user.getName()));
        Globle.USER_CACHE.putIfAbsent(newUser.getId(), newUser);
    }

    public void update(SysUser user) {
        sysUserMapper.updateById(user);
        Globle.USER_CACHE.putIfAbsent(user.getId(), user);
    }

    /**
     * 删除用户信息
     */
    public void del(int id) {
        sysUserMapper.deleteById(id);
        Globle.USER_CACHE.remove(id);
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

    public void modifyAllowance(int userId, Integer allowance, Integer account) {
        SysUser sysUser = new SysUser();
        sysUser.setAllowance(allowance);
        sysUser.setAccountId(account);
        sysUserMapper.update(sysUser, new QueryWrapper<SysUser>().eq("id", userId));
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
