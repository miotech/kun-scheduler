package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class LessThanOrEqualsAssertion extends FixedValueAssertion {

    @JsonCreator
    public LessThanOrEqualsAssertion(@JsonProperty("expectedType") String expectedType,
                                     @JsonProperty("expectedValue") String expectedValue) {
        super(expectedType, expectedValue, ComparisonOperator.LESS_THAN_OR_EQUALS);
    }

    @Override
    public boolean doFixValueAssert(MetricsCollectedResult<String> metricsCollectedResult) {
        String expectedValue = getExpectedValue();
        String originalValue = metricsCollectedResult.getValue();
        return Double.parseDouble(originalValue) <= Double.parseDouble(expectedValue);
    }

}