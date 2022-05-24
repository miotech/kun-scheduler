package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.datadiscovery.service.SecurityRpcClient;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = DataDiscoveryTestBase.TestConfiguration.class)
public abstract class DataDiscoveryTestBase extends KunAppTestBase {
    @MockBean
    protected SecurityRpcClient securityRpcClient;
    @MockBean
    protected DeployedTaskFacade deployedTaskFacade;
    @MockBean
    protected WorkflowClient workflowClient;
    @MockBean
    protected RestTemplate restTemplate;

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
            "com.miotech.kun.datadiscovery",
    })
    public static class TestConfiguration {

    }
}
