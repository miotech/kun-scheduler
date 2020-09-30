package com.miotech.kun.security;

import com.miotech.kun.security.common.AuthenticateInterceptor;
import com.miotech.kun.security.common.LogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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

    @Value("${security.base-url:http://kun-security:8084}")
    String securityBaseUrl;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthenticateInterceptor authenticateInterceptor = new AuthenticateInterceptor();
        authenticateInterceptor.setSecurityBaseUrl(securityBaseUrl);
        registry.addInterceptor(authenticateInterceptor);
        registry.addInterceptor(new LogInterceptor());
    }
}
