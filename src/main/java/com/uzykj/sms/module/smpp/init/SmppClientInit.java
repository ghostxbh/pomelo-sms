package com.uzykj.sms.module.smpp.init;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uzykj.sms.core.domain.SmsAccount;
import com.uzykj.sms.core.enums.ChannelTypeEnum;
import com.uzykj.sms.core.mapper.SmsAccountMapper;
import com.uzykj.sms.core.mapper.SmsCollectMapper;
import com.uzykj.sms.core.mapper.SmsDetailsMapper;
import com.uzykj.sms.module.smpp.hanlder.SmppBusinessHandler;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author ghostxbh
 * @since 2020-08-08
 */
@Component
public class SmppClientInit {

    public static EndpointManager manager = EndpointManager.INS;

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
    public void init() {
        QueryWrapper<SmsAccount> queryWrapper = new QueryWrapper<SmsAccount>().eq("enabled", 1).eq("channel_type", ChannelTypeEnum.SMPP);
        List<SmsAccount> availableAccounts = smsAccountMapper.selectList(queryWrapper);
        Optional.ofNullable(availableAccounts)
                .orElse(new ArrayList<SmsAccount>(0))
                .forEach((account) -> {
                    SMPPClientEndpointEntity entity = new SMPPClientEndpointEntity();

                    String code = account.getCode();
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
//                    entity.setInterfaceVersion((byte) 32);

                    List<BusinessHandlerInterface> businessHandlers = new ArrayList<BusinessHandlerInterface>();
                    businessHandlers.add(new SmppBusinessHandler(smsDetailsMapper, smsCollectMapper));

                    entity.setBusinessHandlerSet(businessHandlers);

                    manager.addEndpointEntity(entity);
                    try {
                        manager.openAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    manager.startConnectionCheckTask();
                });
    }

    public void rebot() {
        this.destroy();
        this.init();
    }


    @PreDestroy
    public void destroy() {
        manager.close();
    }
}
