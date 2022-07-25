package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class NotEqualsAssertion extends FixedValueAssertion {

    @JsonCreator
    public NotEqualsAssertion(@JsonProperty("expectedValue") String expectedValue) {
        super(expectedValue, ComparisonOperator.NOT_EQUALS);
    }

    @Override
    public boolean doFixValueAssert(MetricsCollectedResult<String> metricsCollectedResult) {
        String expectedValue = getExpectedValue();
        String originalValue = metricsCollectedResult.getValue();
        return Double.parseDouble(expectedValue) != Double.parseDouble(originalValue);
    }
}
