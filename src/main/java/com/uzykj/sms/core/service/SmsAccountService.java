package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.domain.dto.SmsAccountDto;
import com.uzykj.sms.core.mapper.SmsAccountMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.module.smpp.init.SmppClientInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsAccountService {

    @Autowired
    private SmsAccountMapper smsAccountMapper;
    @Autowired
    private SmsDetailsMapper smsDetailsMapper;

    public void add(SmsAccount account) throws Exception {
        smsAccountMapper.insert(account);
        SmppClientInit clientInit = new SmppClientInit(smsAccountMapper, smsDetailsMapper);
        clientInit.rebot();
    }

    public void update(SmsAccount account) throws Exception {
        smsAccountMapper.updateById(account);
        SmppClientInit clientInit = new SmppClientInit(smsAccountMapper, smsDetailsMapper);
        clientInit.rebot();
    }

    public void del(int id) throws Exception {
        smsAccountMapper.deleteById(id);
        SmppClientInit clientInit = new SmppClientInit(smsAccountMapper, smsDetailsMapper);
        clientInit.rebot();
    }

    public SmsAccount get(int id) {
        return smsAccountMapper.selectOne(new QueryWrapper<SmsAccount>().eq("id", id));
    }

    public SmsAccount getByCode(String code) {
        return smsAccountMapper.selectOne(new QueryWrapper<SmsAccount>().eq("code", code));
    }

    public Page<SmsAccount> getAll(PageDto pageDto, SmsAccountDto accountDto) {
        if (pageDto == null && accountDto == null) {
            Page<SmsAccount> page = new Page<SmsAccount>();
            List<SmsAccount> enabledList = smsAccountMapper.selectList(new QueryWrapper<SmsAccount>().eq("enabled", 1));
            page.setRecords(enabledList);
            page.setTotal(enabledList.size());
            return page;
        }
        QueryWrapper<SmsAccount> query = getQuery(accountDto);
        int skip = (pageDto.getPage() - 1) * pageDto.getPageSize();
        Page<SmsAccount> page = new Page<SmsAccount>(skip, pageDto.getPageSize());
        return smsAccountMapper.selectPage(page, query);
    }

    public int getAllCount(SmsAccountDto accountDto) {
        QueryWrapper<SmsAccount> query = getQuery(accountDto);
        return smsAccountMapper.selectCount(query);
    }

    private QueryWrapper<SmsAccount> getQuery(SmsAccountDto accountDto) {
        QueryWrapper<SmsAccount> query = new QueryWrapper<SmsAccount>();
        query.orderByDesc("create_time");
        if (accountDto.getCode() != null && !"".equals(accountDto.getCode())) {
            query.eq("code", accountDto.getCode());
        }
        if (accountDto.getEnabled() != null && !"".equals(accountDto.getEnabled())) {
            query.eq("enabled", accountDto.getEnabled());
        }
        if (accountDto.getSystemId() != null && !"".equals(accountDto.getSystemId())) {
            query.eq("system_id", accountDto.getSystemId());
        }
        if (accountDto.getIsInvalid() != null && !"".equals(accountDto.getIsInvalid())) {
            query.eq("is_invalid", accountDto.getIsInvalid());
        }
        return query;
    }
}
