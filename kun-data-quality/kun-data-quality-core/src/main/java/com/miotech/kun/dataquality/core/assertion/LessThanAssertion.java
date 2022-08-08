package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class LessThanAssertion extends FixedValueAssertion {

    @JsonCreator
    public LessThanAssertion(@JsonProperty("expectedValue") String expectedValue) {
        super(expectedValue, ComparisonOperator.LESS_THAN);
    }

    @Override
    public boolean doFixValueAssert(MetricsCollectedResult<String> metricsCollectedResult) {
        String expectedValue = getExpectedValue();
        String originalValue = metricsCollectedResult.getValue();
        return Double.parseDouble(originalValue) < Double.parseDouble(expectedValue);
    }

}
