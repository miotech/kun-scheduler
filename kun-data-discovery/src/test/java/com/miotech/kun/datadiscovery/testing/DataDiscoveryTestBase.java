package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.datadiscovery.service.SecurityRpcClient;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.workflow.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = DataDiscoveryTestBase.TestConfiguration.class)
@Slf4j
public abstract class DataDiscoveryTestBase extends KunAppTestBase {
    @MockBean
    protected SecurityRpcClient securityRpcClient;
    @MockBean
    protected DeployedTaskFacade deployedTaskFacade;
    @MockBean
    protected WorkflowClient workflowClient;
    @MockBean
    protected RestTemplate restTemplate;

    @Value("${metadata.base-url:localhost:8084}")
    protected String url;

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
            "com.miotech.kun.datadiscovery",
    })
    @Import(DiscoveryConfig.class)
    public static class TestConfiguration {

    }
}
