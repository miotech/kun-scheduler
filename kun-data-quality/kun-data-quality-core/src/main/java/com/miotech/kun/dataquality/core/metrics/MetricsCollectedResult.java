package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;

import java.time.OffsetDateTime;

public class MetricsCollectedResult<T> {

    private final Metrics metrics;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private final OffsetDateTime collectedAt;

    private final T value;

    @JsonCreator
    public MetricsCollectedResult(@JsonProperty("metrics") Metrics metrics,
                                  @JsonProperty("collectedAt") OffsetDateTime collectedAt,
                                  @JsonProperty("value") T value) {
        this.metrics = metrics;
        this.collectedAt = collectedAt;
        this.value = value;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public OffsetDateTime getCollectedAt() {
        return collectedAt;
    }

    public T getValue() {
        return value;
    }

}
