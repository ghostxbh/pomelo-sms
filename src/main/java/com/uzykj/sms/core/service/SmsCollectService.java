package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.SmsStatus;
import com.uzykj.sms.core.domain.*;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            if (userName != null && !("").equals(userName)) {
                SysUser selectOne = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("name", userName));
                query.eq("user_id", selectOne.getId());
            }
        }
        Page<SmsCollect> page = smsCollectMapper.selectPage(new Page<SmsCollect>(pageDto.getPage(), pageDto.getPageSize()), query);
        List<SmsCollect> collectList = Optional.ofNullable(page.getRecords())
                .orElse(new ArrayList<SmsCollect>(0))
                .stream()
                .peek(collect -> {
                    collect.setStatus(SmsStatus.switchSmsStatus(collect.getStatus()));
                    collect.setUser(sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("id", collect.getUserId())));
                }).collect(Collectors.toList());
        page.setRecords(collectList);
        return page;
    }


    public int successCountToday(int userId) {
        return smsCollectMapper.successCountByDate(userId);
    }

    public int failCountToday(int userId) {
        return smsCollectMapper.failCountByDate(userId);
    }
}
