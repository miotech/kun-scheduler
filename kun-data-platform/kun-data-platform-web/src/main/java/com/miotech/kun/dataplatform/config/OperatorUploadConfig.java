package com.miotech.kun.dataplatform.config;

import com.miotech.kun.workflow.client.operator.OperatorUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${workflow.enabled:true}")
public class OperatorUploadConfig {

    @Value("${workflow.baseUrl}")
    private String workflowUrl;

    @Bean
    OperatorUpload getOperatorUpload(){
        return new OperatorUpload(workflowUrl);
    }

}
