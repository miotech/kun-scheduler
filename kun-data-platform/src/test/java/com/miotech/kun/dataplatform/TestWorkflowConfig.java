package com.miotech.kun.dataplatform;

import com.miotech.kun.workflow.client.WorkflowClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestWorkflowConfig {
    @Bean
    public WorkflowClient getWorkflowClient() {
        return Mockito.mock(WorkflowClient.class);
    }
}
