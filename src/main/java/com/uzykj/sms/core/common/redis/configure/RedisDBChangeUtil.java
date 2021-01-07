package com.uzykj.sms.core.common.redis.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * redis配置
 *
 * @author ghostxbh
 */

@Component
public class RedisDBChangeUtil {
 
    @Autowired
    private RedisTemplate redisTemplate;
 
    public void setDataBase(int num) {
        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
        if (connectionFactory != null && num != connectionFactory.getDatabase()) {
            connectionFactory.setDatabase(num);
            connectionFactory.setValidateConnection(true);
            this.redisTemplate.setConnectionFactory(connectionFactory);
            connectionFactory.resetConnection();
        }
    }
}