package com.uzykj.sms.module.smpp.init;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.mapper.SmsAccountMapper;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.module.smpp.hanlder.SmppBusinessHandler;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
@Component
public class SmppClientInit {

    private final EndpointManager manager = EndpointManager.INS;

    @Autowired
    private SmsAccountMapper smsAccountMapper;
    @Autowired
    private SmsDetailsMapper smsDetailsMapper;
    @Autowired
    private SmsCollectMapper smsCollectMapper;

    public SmppClientInit(SmsAccountMapper smsAccountMapper, SmsDetailsMapper smsDetailsMapper) {
        this.smsAccountMapper = smsAccountMapper;
        this.smsDetailsMapper = smsDetailsMapper;
    }

    @PostConstruct
    public void init() throws Exception {
        List<SmsAccount> availableAccounts = smsAccountMapper.selectList(new QueryWrapper<SmsAccount>().eq("enabled", 1));
        if (CollectionUtils.isNotEmpty(availableAccounts)) {
            for (SmsAccount account : availableAccounts) {
                this.connectByAccount(account);
            }
            manager.openAll();
            manager.startConnectionCheckTask();
        }
    }

    public void rebot() throws Exception {
        this.destroy();
        this.init();
    }


    @PreDestroy
    public void destroy() {
        manager.close();
    }

    private void connectByAccount(SmsAccount account) {
        for (int i = 0; i < 6; i++) {
            SMPPClientEndpointEntity entity = new SMPPClientEndpointEntity();

            String code = account.getCode();
            String systemId = account.getSystemId();
            String password = account.getPassword();
            String url = account.getUrl();
            int port = account.getPort();

            if (i == 0) {
                entity.setMaxChannels((short) 3);
                entity.setId(code);
                entity.setChannelType(EndpointEntity.ChannelType.UP);
            } else {
                entity.setChannelType(EndpointEntity.ChannelType.DOWN);
                entity.setId(code + "_tx_" + i);
                List<BusinessHandlerInterface> businessHandlers = new ArrayList<BusinessHandlerInterface>();
                businessHandlers.add(new SmppBusinessHandler(smsDetailsMapper, smsCollectMapper));
                entity.setBusinessHandlerSet(businessHandlers);
            }
            entity.setHost(url);
            entity.setPort(port);
            entity.setSystemId(systemId);
            entity.setPassword(password);


            entity.setRetryWaitTimeSec((short) 100);
            entity.setUseSSL(false);
            entity.setReSendFailMsg(false);

            manager.addEndpointEntity(entity);
        }
    }
}
