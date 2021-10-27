package work.pomelo.admin.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * @author ghostxbh
 * @date 2020/8/11
 * 
 */
@Configuration
public class ErrorPageConfig implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage[] errorPage = new ErrorPage[3];
        errorPage[0] = new ErrorPage(HttpStatus.NOT_FOUND, "/404");
        errorPage[1] = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/");
        errorPage[2] = new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/");
        registry.addErrorPages(errorPage);
    }
}
