package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;

import java.time.OffsetDateTime;

public abstract class MetricsCollectedResult<T> {

    private final Metrics metrics;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private final OffsetDateTime collectedAt;

    public MetricsCollectedResult(Metrics metrics, OffsetDateTime collectedAt) {
        this.metrics = metrics;
        this.collectedAt = collectedAt;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public OffsetDateTime getCollectedAt() {
        return collectedAt;
    }

    public abstract T getResult();

}
