package com.miotech.kun.security;

import com.miotech.kun.security.filter.AuthenticateInterceptor;
import com.miotech.kun.security.filter.CachingFilter;
import com.miotech.kun.security.filter.LogInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthenticateInterceptor authenticateInterceptor = new AuthenticateInterceptor();
        registry.addInterceptor(authenticateInterceptor);
        registry.addInterceptor(new LogInterceptor());
    }

    @Bean
    public FilterRegistrationBean<CachingFilter> cachingFilter() {
        FilterRegistrationBean<CachingFilter> registration = new FilterRegistrationBean<>();
        CachingFilter cachingFilter = new CachingFilter();
        registration.setFilter(cachingFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
