package work.pomelo.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "work.pomelo")
@MapperScan("work.pomelo.admin.mapper")
@EnableCaching
public class PomeloSmsAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(PomeloSmsAdminApplication.class, args);
    }
}
