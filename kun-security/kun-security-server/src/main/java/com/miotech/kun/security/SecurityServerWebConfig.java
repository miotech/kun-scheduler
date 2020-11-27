package com.miotech.kun.security;

import com.miotech.kun.security.filter.LogFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: Jie Chen
 * @created: 2020/9/22
 */
@Configuration
public class SecurityServerWebConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("dispatcherServletRegistration")
    private ServletRegistrationBean servletRegistrationBean;

    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        FilterRegistrationBean<LogFilter> registration = new FilterRegistrationBean<>();
        registration.addServletRegistrationBeans(servletRegistrationBean);
        LogFilter cachingFilter = new LogFilter();
        registration.setFilter(cachingFilter);
        registration.setOrder(1);
        return registration;
    }
}
