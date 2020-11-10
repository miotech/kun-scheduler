package com.miotech.kun.security;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * @author: Jie Chen
 * @created: 2020/9/22
 */
@SpringBootApplication
@ComponentScan(excludeFilters  = {@ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class})})
@EnableDubbo(scanBasePackages = "com.miotech.kun.security.facade")
public class SecurityServer {

    public static void main(String[] args) {
        SpringApplication.run(SecurityServer.class);
    }
}
