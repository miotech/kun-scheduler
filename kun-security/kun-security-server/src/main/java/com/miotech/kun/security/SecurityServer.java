package com.miotech.kun.security;

import com.miotech.kun.security.saml2.Saml2RelyingPartyMappings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * @author: Jie Chen
 * @created: 2020/9/22
 */
@SpringBootApplication
@ComponentScan(excludeFilters  = {@ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class})})
@EnableConfigurationProperties({Saml2RelyingPartyMappings.class})
public class SecurityServer {

    public static void main(String[] args) {
        SpringApplication.run(SecurityServer.class);
    }
}
