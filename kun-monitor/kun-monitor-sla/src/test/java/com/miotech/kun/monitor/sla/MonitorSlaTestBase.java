package com.miotech.kun.monitor.sla;

import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.monitor.sla.config.TestFacadeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = MonitorSlaTestBase.TestConfig.class)
@EnableScheduling
@EnableAsync
@Slf4j
public abstract class MonitorSlaTestBase extends KunAppTestBase {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
            "com.miotech.kun.dataplatform",
            "com.miotech.kun.monitor"
    })
    @Import(TestFacadeConfig.class)
    public static class TestConfig {
        @Bean
        public RestTemplate getRestTemplate() {
            return new RestTemplate();
        }
    }
}
