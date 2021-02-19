package com.miotech.kun.dataplatform.config;

import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${workflow.enabled:true}")
public class WorkflowConfig {

    @Value("${workflow.baseUrl}")
    private String workflowUrl;

    @Bean
    public WorkflowClient getWorkflowClient() {
         return new DefaultWorkflowClient(workflowUrl);
    }

    @Value("${workflow.variableNamespace}")
    private String variableNamespace = "dataplatform";

    public String getVariableNamespace() {
        return this.variableNamespace;
    }
}
