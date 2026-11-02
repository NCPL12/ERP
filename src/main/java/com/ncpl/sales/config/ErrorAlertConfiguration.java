package com.ncpl.sales.config;

import java.util.EnumSet;
import java.util.concurrent.Executor;

import javax.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ncpl.sales.service.ErrorAlertService;

@Configuration
public class ErrorAlertConfiguration {
    @Bean("errorAlertExecutor")
    public Executor errorAlertExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ErrorAlert-");
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }

    @Bean
    public FilterRegistrationBean<ErrorAlertFilter> errorAlertFilter(
            ErrorAlertService alertService,
            @Value("${error.alert.cooldown-ms:300000}") long cooldownMillis) {
        FilterRegistrationBean<ErrorAlertFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ErrorAlertFilter(alertService, cooldownMillis));
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR));
        registration.setAsyncSupported(true);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return registration;
    }
}
