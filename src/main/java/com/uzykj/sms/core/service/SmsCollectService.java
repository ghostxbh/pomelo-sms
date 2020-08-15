package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.SmsStatus;
import com.uzykj.sms.core.domain.*;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.domain.dto.SmsCollectDto;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmsCollectService {

    @Autowired
    private SmsCollectMapper smsCollectMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    public Page<SmsCollectDto> getAll(SysUser user, PageDto pageDto, String userName) {
        QueryWrapper<SmsCollect> query = new QueryWrapper<SmsCollect>();
        query.orderByDesc("create_time");
        if ("admin".equals(user.getRole())) {
            if (userName != null && userName != "") {
                SysUser selectOne = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("name", userName));
                query.eq("user_id", selectOne.getId());
            }
        }
        int skip = (pageDto.getPage() - 1) * pageDto.getPageSize();
        Page<SmsCollect> smsCollectPage = smsCollectMapper.selectPage(new Page<SmsCollect>(skip, pageDto.getPageSize()), query);
        List<SmsCollect> collectList = smsCollectPage.getRecords();
        List<SmsCollectDto> collectDtos = new ArrayList<>(collectList.size());
        collectList.forEach(collect -> {
            collect.setStatus(SmsStatus.switchSmsStatus(collect.getStatus()));
            SysUser sysUser = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("id", collect.getUserId()));
            SmsCollectDto dto = new SmsCollectDto(collect, sysUser);
            collectDtos.add(dto);
        });

        Page<SmsCollectDto> page = new Page<SmsCollectDto>();
        page.setRecords(collectDtos);
        page.setTotal(smsCollectPage.getTotal());
        page.setPages(smsCollectPage.getPages());
        page.setCurrent(smsCollectPage.getCurrent());
        page.setSize(smsCollectPage.getSize());
        return page;
    }


    public int successCountToday(int userId) {
        return smsCollectMapper.successCountByDate(userId);
    }

    public int failCountToday(int userId) {
        return smsCollectMapper.failCountByDate(userId);
    }
}
