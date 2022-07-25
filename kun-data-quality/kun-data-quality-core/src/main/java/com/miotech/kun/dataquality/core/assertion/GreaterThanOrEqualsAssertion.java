package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class GreaterThanOrEqualsAssertion extends FixedValueAssertion {

    @JsonCreator
    public GreaterThanOrEqualsAssertion(@JsonProperty("expectedValue") String expectedValue) {
        super(expectedValue, ComparisonOperator.GREATER_THAN_OR_EQUALS);
    }

    @Override
    public boolean doFixValueAssert(MetricsCollectedResult<String> metricsCollectedResult) {
        String expectedValue = getExpectedValue();
        String originalValue = metricsCollectedResult.getValue();
        return Double.parseDouble(originalValue) >= Double.parseDouble(expectedValue);
    }
}
