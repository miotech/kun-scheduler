package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class AssertionSample {

    private final MetricsCollectedResult currentValue;

    private final MetricsCollectedResult benchmarkValue;

    public AssertionSample(MetricsCollectedResult currentValue, MetricsCollectedResult benchmarkValue) {
        this.currentValue = currentValue;
        this.benchmarkValue = benchmarkValue;
    }

    public static AssertionSample of(MetricsCollectedResult currentValue, MetricsCollectedResult benchmarkValue) {
        return new AssertionSample(currentValue, benchmarkValue);
    }

    public MetricsCollectedResult getCurrentValue() {
        return currentValue;
    }

    public MetricsCollectedResult getBenchmarkValue() {
        return benchmarkValue;
    }
}
