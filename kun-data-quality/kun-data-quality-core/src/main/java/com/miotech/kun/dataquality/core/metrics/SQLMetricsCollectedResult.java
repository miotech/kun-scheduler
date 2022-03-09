package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class SQLMetricsCollectedResult extends MetricsCollectedResult<String> {

    private final String value;

    @JsonCreator
    public SQLMetricsCollectedResult(@JsonProperty("metrics") Metrics metrics,
                                     @JsonProperty("collectedAt") OffsetDateTime collectedAt,
                                     @JsonProperty("value") String value) {
        super(metrics, collectedAt);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getResult() {
        return getValue();
    }

}
