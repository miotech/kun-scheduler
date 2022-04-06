package com.miotech.kun.datadashboard;

import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.datadashboard.config.TestFacadeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootTest(classes = DataDashboardTestBase.TestConfig.class)
@EnableScheduling
@EnableAsync
@Slf4j
public abstract class DataDashboardTestBase extends KunAppTestBase {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.datadashboard",
            "com.miotech.kun.security",
    })

    @Import(TestFacadeConfig.class)
    public static class TestConfig {

    }
}
