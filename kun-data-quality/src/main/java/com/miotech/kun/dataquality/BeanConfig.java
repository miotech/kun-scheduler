package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.utils.WorkflowUtils;
import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.core.model.common.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
public class BeanConfig {

    @Value("${workflow.url}")
    String workflowUrl;

    @Autowired
    WorkflowUtils workflowUtils;

    @Bean
    WorkflowClient getWorkflowClient() {
        return new DefaultWorkflowClient(workflowUrl);
    }

    @Bean
    Operator getOperator() {
        List<Param> params = workflowUtils.getInitParams("");
        return Operator.newBuilder()
                .withName(DataQualityConfiguration.WORKFLOW_OPERATOR_NAME)
                .withDescription("default operator")
                .withClassName("com.miotech.kun.workflow.operator.DataQualityOperator")
                .withPackagePath("file:/server/lib/data-quality-operators.jar")
                .withParams(params)
                .build();
    }
}
