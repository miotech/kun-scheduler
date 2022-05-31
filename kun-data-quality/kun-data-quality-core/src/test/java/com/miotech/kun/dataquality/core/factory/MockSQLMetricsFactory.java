package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;

public class MockSQLMetricsFactory {

    private MockSQLMetricsFactory() {
    }

    public static SQLMetrics create() {
        return SQLMetrics.newBuilder()
                .withName("sql metrics")
                .withDescription("desc")
                .withGranularity(Metrics.Granularity.CUSTOM)
                .withDataset(null)
                .withSql("select count(1) c from demo")
                .withField("c")
                .build();
    }

}
