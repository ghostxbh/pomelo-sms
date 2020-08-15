package com.uzykj.sms;

import com.uzykj.sms.module.smpp.queue.SmsSendRunner;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.uzykj.sms.core.mapper")
public class SmsSystemApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SmsSystemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        SmsSendRunner.getInstance().start();
    }
}
