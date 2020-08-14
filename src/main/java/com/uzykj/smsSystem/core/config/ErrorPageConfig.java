package com.uzykj.smsSystem.core.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ErrorPageConfig implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage[] errorPage = new ErrorPage[2];
        errorPage[0] = new ErrorPage(HttpStatus.NOT_FOUND, "/404");
        errorPage[1] = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/");
        registry.addErrorPages(errorPage);
    }
}
