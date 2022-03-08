package com.miotech.kun.dataquality.core.metrics;

import java.time.OffsetDateTime;

public class SQLMetricsCollectedResult extends MetricsCollectedResult<String> {

    private final String value;

    public SQLMetricsCollectedResult(Metrics metrics, OffsetDateTime collectedAt, String value) {
        super(metrics, collectedAt);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getResult() {
        return value;
    }

}
