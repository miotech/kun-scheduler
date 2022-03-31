package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class RiseAssertion extends VolatilityAssertion {

    @JsonCreator
    public RiseAssertion(@JsonProperty("expectedType") String expectedType,
                         @JsonProperty("expectedValue") String expectedValue,
                         @JsonProperty("comparisonPeriod") ComparisonPeriod comparisonPeriod) {
        super(expectedType, expectedValue, ComparisonOperator.RISE, comparisonPeriod);
    }

    @Override
    public boolean doVolatilityAssert(MetricsCollectedResult<String> currentValue, MetricsCollectedResult<String> benchmarkValue) {
        double expected = Double.parseDouble(getExpectedValue());
        double current = Double.parseDouble(currentValue.getValue());
        double baseline = Double.parseDouble(benchmarkValue.getValue());
        double volatility = ((current - baseline) / baseline) * 100;
        return volatility >= 0 && volatility <= expected;
    }
}
