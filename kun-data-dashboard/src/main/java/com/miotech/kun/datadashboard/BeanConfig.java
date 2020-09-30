package com.miotech.kun.datadashboard;

import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
public class BeanConfig {

    @Value("${workflow.base-url:http://kun-workflow:8088}")
    String workflowUrl;

    @Bean
    WorkflowClient getWorkflowClient() {
        return new DefaultWorkflowClient(workflowUrl);
    }
}
