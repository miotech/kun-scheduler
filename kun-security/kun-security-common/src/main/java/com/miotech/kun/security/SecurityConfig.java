package com.miotech.kun.security;

import com.miotech.kun.security.filter.AuthenticateFilter;
import com.miotech.kun.security.filter.AuthenticateInterceptor;
import com.miotech.kun.security.filter.LogFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: Jie Chen
 * @created: 2020/8/7
 */
@Configuration
@ConditionalOnExpression("#{environment.getActiveProfiles()[0] != 'test'}")
public class SecurityConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("dispatcherServletRegistration")
    private ServletRegistrationBean servletRegistrationBean;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthenticateInterceptor authenticateInterceptor = new AuthenticateInterceptor();
        registry.addInterceptor(authenticateInterceptor);
    }

    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        FilterRegistrationBean<LogFilter> registration = new FilterRegistrationBean<>();
        registration.addServletRegistrationBeans(servletRegistrationBean);
        LogFilter cachingFilter = new LogFilter();
        registration.setFilter(cachingFilter);
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthenticateFilter> authenticateFilter() {
        FilterRegistrationBean<AuthenticateFilter> registration = new FilterRegistrationBean<>();
        registration.addServletRegistrationBeans(servletRegistrationBean);
        registration.addUrlPatterns("/kun");
        AuthenticateFilter cachingFilter = new AuthenticateFilter();
        registration.setFilter(cachingFilter);
        registration.setOrder(0);
        return registration;
    }
}
