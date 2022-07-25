package com.miotech.kun.workflow.operator.mock;

import com.miotech.kun.dataquality.core.metrics.SQLMetrics;

public class MockSQLMetricsFactory {

    private MockSQLMetricsFactory() {
    }

    public static SQLMetrics create() {
        return SQLMetrics.newBuilder()
                .withSql("select count(1) c from demo")
                .withField("c")
                .build();
    }

}
