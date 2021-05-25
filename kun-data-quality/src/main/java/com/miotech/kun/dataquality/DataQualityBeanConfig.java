package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.utils.WorkflowUtils;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
public class DataQualityBeanConfig {

    @Value("${workflow.base-url:http://kun-workflow:8088}")
    String workflowUrl;

    @Autowired
    WorkflowUtils workflowUtils;

    @Bean
    OperatorUpload getOperatorUpload(){
        return new OperatorUpload(workflowUrl);
    }

    @Bean
    Operator getOperator() {
        return Operator.newBuilder()
                .withName(DataQualityConfiguration.WORKFLOW_OPERATOR_NAME)
                .withDescription("Data Quality Operator")
                .withClassName("com.miotech.kun.workflow.operator.DataQualityOperator")
                .build();
    }
}
