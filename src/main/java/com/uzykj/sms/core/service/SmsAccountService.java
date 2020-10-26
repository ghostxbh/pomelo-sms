package com.uzykj.sms.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.domain.dto.PageDto;
import com.uzykj.sms.core.domain.dto.SmsAccountDto;
import com.uzykj.sms.core.enums.ChannelTypeEnum;
import com.uzykj.sms.core.mapper.SmsAccountMapper;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.module.smpp.hanlder.SmppBusinessHandler;
import com.uzykj.sms.module.smpp.init.SmppClientInit;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmsAccountService {

    @Autowired
    private SmsAccountMapper smsAccountMapper;
    @Autowired
    private SmsDetailsMapper smsDetailsMapper;
    @Autowired
    private SmsCollectMapper smsCollectMapper;

    public void add(SmsAccount account) {
        smsAccountMapper.insert(account);
        if (account.getChannelType().equals(ChannelTypeEnum.SMPP)) {
            SmppClientInit clientInit = new SmppClientInit(smsAccountMapper, smsDetailsMapper);
            clientInit.rebot();
        }
        Globle.updateCache();
    }

    public void update(SmsAccount account) {
        smsAccountMapper.updateById(account);
        if (account.getChannelType().equals(ChannelTypeEnum.SMPP)) {
            SmppClientInit clientInit = new SmppClientInit(smsAccountMapper, smsDetailsMapper);
            clientInit.rebot();
        }
        Globle.updateCache();
    }

    public void del(int id) {
        smsAccountMapper.deleteById(id);
        SmppClientInit clientInit = new SmppClientInit(smsAccountMapper, smsDetailsMapper);
        clientInit.rebot();
        Globle.updateCache();
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

    public boolean check(String code) {
        EndpointEntity entity = SmppClientInit.manager.getEndpointEntity(code);
        return entity != null && entity.getId() != null;
    }

    public void refrensh(String code) {
        EndpointManager manager = SmppClientInit.manager;
        SMPPClientEndpointEntity entity = new SMPPClientEndpointEntity();

        SmsAccount account = Globle.ACCOUNT_CACHE.get(code);

        String systemId = account.getSystemId();
        String password = account.getPassword();
        String url = account.getUrl();
        int port = account.getPort();

        entity.setId(code);
        entity.setHost(url);
        entity.setPort(port);
        entity.setSystemId(systemId);
        entity.setPassword(password);
        entity.setChannelType(EndpointEntity.ChannelType.DUPLEX);
        entity.setMaxChannels((short) 3);
        entity.setRetryWaitTimeSec((short) 100);
        entity.setUseSSL(false);
        entity.setReSendFailMsg(false);

        List<BusinessHandlerInterface> businessHandlers = new ArrayList<BusinessHandlerInterface>();
        businessHandlers.add(new SmppBusinessHandler(smsDetailsMapper, smsCollectMapper));

        entity.setBusinessHandlerSet(businessHandlers);

        manager.addEndpointEntity(entity);
        try {
            manager.openEndpoint(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        manager.startConnectionCheckTask();
    }
}
