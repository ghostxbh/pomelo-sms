package com.uzykj.smsSystem.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.smsSystem.core.domain.*;
import com.uzykj.smsSystem.core.domain.dto.PageDto;
import com.uzykj.smsSystem.core.mapper.SmsCollectMapper;
import com.uzykj.smsSystem.core.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsCollectService {

    @Autowired
    private SmsCollectMapper smsCollectMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    public Page<SmsCollect> getAll(SysUser user, PageDto pageDto, String userName) {
        QueryWrapper<SmsCollect> query = new QueryWrapper<SmsCollect>();
        query.orderByDesc("create_time");
        if ("admin".equals(user.getRole())) {
            if (userName != null && userName != "") {
                SysUser selectOne = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("name", userName));
                query.eq("user_id", selectOne.getId());
            }
        }
        int skip = (pageDto.getPage() - 1) * pageDto.getPageSize();
        Page<SmsCollect> page = new Page<SmsCollect>(skip, pageDto.getPageSize());
        return smsCollectMapper.selectPage(page, query);
    }


    public int successCountToday(int userId) {
        return smsCollectMapper.successCountByDate(userId);
    }

    public int failCountToday(int userId) {
        return smsCollectMapper.failCountByDate(userId);
    }
}
