package com.miotech.kun.dataplatform.config;

import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowConfig {
    @Value("${workflow.base-url}")
    private String workflowUrl;

    @ConditionalOnExpression("${workflow.enabled:true}")
    @Bean
    public WorkflowClient getWorkflowClient() {
        return new DefaultWorkflowClient(workflowUrl);
    }

    @Value("${workflow.variable-namespace:dataplatform}")
    private String variableNamespace;

    public String getVariableNamespace() {
        return this.variableNamespace;
    }
}
