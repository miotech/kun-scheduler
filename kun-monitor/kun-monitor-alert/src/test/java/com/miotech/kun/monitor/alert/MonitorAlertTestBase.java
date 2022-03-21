package com.miotech.kun.monitor.alert;

import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.monitor.alert.config.TestWorkflowConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = MonitorAlertTestBase.TestBaseConfig.class)
@Slf4j
public abstract class MonitorAlertTestBase extends KunAppTestBase {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
            "com.miotech.kun.dataplatform",
            "com.miotech.kun.monitor"
    })
    @Import({TestWorkflowConfig.class})
    public static class TestBaseConfig {

    }
}
