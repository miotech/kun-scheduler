package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.dataquality.core.expectation.Dataset;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property = "metricsType", visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = SQLMetrics.class, name = "SQL")
})
public abstract class Metrics<R> {

    private final MetricsType metricsType;

    public Metrics(MetricsType metricsType) {
        this.metricsType = metricsType;
    }

    public MetricsType getMetricsType() {
        return metricsType;
    }

    public abstract MetricsCollectedResult<R> collect(CollectContext context);

    public enum Granularity {
        TABLE, FIELD, CUSTOM;
    }

}
