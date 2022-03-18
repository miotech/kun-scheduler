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

    private final String name;

    private final String description;

    private final Granularity granularity;

    private final Dataset dataset;

    public Metrics(MetricsType metricsType, String name, String description, Granularity granularity, Dataset dataset) {
        this.metricsType = metricsType;
        this.name = name;
        this.description = description;
        this.granularity = granularity;
        this.dataset = dataset;
    }

    public MetricsType getMetricsType() {
        return metricsType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Granularity getGranularity() {
        return granularity;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public abstract MetricsCollectedResult<R> collect();

    public enum Granularity {
        TABLE, FIELD, CUSTOM;
    }

}
