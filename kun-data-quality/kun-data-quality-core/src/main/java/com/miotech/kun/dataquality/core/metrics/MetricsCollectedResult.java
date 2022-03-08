package com.miotech.kun.dataquality.core.metrics;

import java.time.OffsetDateTime;

public abstract class MetricsCollectedResult<T> {

    private final Metrics metrics;

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
