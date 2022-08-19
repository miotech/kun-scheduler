package com.miotech.kun.security;

import com.miotech.kun.commons.testing.KunAppTestBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = SecurityTestBase.TestConfig.class)
@EnableScheduling
@EnableAsync
@Slf4j
public abstract class SecurityTestBase extends KunAppTestBase {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
    })
    public static class TestConfig {

    }

    @DynamicPropertySource
    static void registerDatabase(DynamicPropertyRegistry registry) {
        // session properties
        registry.add("spring.session.datasource.url", () -> postgresContainer.getJdbcUrl() + "&stringtype=unspecified");
        registry.add("spring.session.datasource.username", postgresContainer::getUsername);
        registry.add("spring.session.datasource.password", postgresContainer::getPassword);
        registry.add("spring.session.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
