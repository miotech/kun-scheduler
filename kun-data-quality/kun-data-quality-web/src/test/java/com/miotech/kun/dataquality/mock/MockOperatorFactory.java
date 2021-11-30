package com.miotech.kun.dataquality.mock;

import com.google.common.collect.Lists;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

public class MockOperatorFactory {


    public static Operator createOperator() {
        ConfigKey configKey = new ConfigKey();
        configKey.setName("sparkSQL");
        configKey.setDisplayName("sql");
        configKey.setReconfigurable(true);
        configKey.setType(ConfigDef.Type.STRING);
        Operator operator = Operator.newBuilder()
                .withId(WorkflowIdGenerator.nextOperatorId())
                .withName("SparkSQL")
                .withClassName("com.miotech.kun.dataplatform.mocking.TestSQLOperator")
                .withConfigDef(Lists.newArrayList(configKey))
                .withDescription("Spark SQL Operator")
                .build();
        return operator;
    }
}
