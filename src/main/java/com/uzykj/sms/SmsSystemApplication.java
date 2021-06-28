package com.uzykj.sms;

import com.uzykj.sms.core.common.Globle;
import com.uzykj.sms.module.sender.SMPPQueueRunner;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author ghostxbh
 */
@SpringBootApplication
@MapperScan("com.uzykj.sms.core.mapper")
@EnableAsync
public class SmsSystemApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SmsSystemApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Globle.initCache();
        SMPPQueueRunner.getInstance().start();
    }
}
