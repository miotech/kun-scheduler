package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class FallAssertion extends VolatilityAssertion {

    @JsonCreator
    public FallAssertion(@JsonProperty("expectedType") String expectedType,
                         @JsonProperty("expectedValue") String expectedValue,
                         @JsonProperty("comparisonPeriod") ComparisonPeriod comparisonPeriod) {
        super(expectedType, expectedValue, ComparisonOperator.ABSOLUTE, comparisonPeriod);
    }

    @Override
    public boolean doAssert(MetricsCollectedResult metricsCollectedResult) {
        return false;
    }
}
