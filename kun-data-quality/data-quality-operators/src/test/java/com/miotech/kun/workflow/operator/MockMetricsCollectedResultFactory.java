package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;

public class MockMetricsCollectedResultFactory {

    private MockMetricsCollectedResultFactory() {
    }

    public static MetricsCollectedResult create() {
        Metrics metrics = SQLMetrics.newBuilder()
                .withSql("select count(1) c from test")
                .withField("c")
                .withName("test metrics")
                .withDescription("desc")
                .withGranularity(Metrics.Granularity.CUSTOM)
                .build();
        return new MetricsCollectedResult(metrics, DateTimeUtils.now(), "0");
    }
}
