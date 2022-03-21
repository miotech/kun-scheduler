package com.miotech.kun.dataplatform;

import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.dataplatform.web.common.tasktemplate.service.TaskTemplateLoader;
import com.miotech.kun.dataplatform.web.config.TestOnlyController;
import com.miotech.kun.dataplatform.web.config.TestWorkflowConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = DataPlatformTestBase.TestConfiguration.class)
@Slf4j
public abstract class DataPlatformTestBase extends KunAppTestBase {

    @Autowired
    private TaskTemplateLoader taskTemplateLoader;

    @BeforeEach
    public void init() {
        taskTemplateLoader.persistTemplates();
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
            "com.miotech.kun.dataplatform",
            "com.miotech.kun.monitor"
    })
    @Import({TestWorkflowConfig.class, TestOnlyController.class})
    public static class TestConfiguration {

    }
}
