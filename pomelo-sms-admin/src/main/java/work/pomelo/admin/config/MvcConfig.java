package work.pomelo.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import work.pomelo.admin.config.interceptor.UserInterceptor;

/**
 * MVC配置
 *
 * @author ghostxbh
 * @date 2021-10-12
 */
@Slf4j
@Configuration
//@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private UserInterceptor userInterceptor;

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**")
//                .addResourceLocations("/static", "/**");
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/user/**")
                .addPathPatterns("/channel/**")
                .addPathPatterns("/manager/**")
                .addPathPatterns("/main/**")
                .addPathPatterns("/sms/**")
                .addPathPatterns("/statis/**")
                .addPathPatterns("/home");
    }
}