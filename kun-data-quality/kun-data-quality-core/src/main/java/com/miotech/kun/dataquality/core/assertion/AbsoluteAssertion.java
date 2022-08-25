package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public class AbsoluteAssertion extends VolatilityAssertion {

    @JsonCreator
    public AbsoluteAssertion(@JsonProperty("expectedValue") String expectedValue,
                             @JsonProperty("comparisonPeriod") ComparisonPeriod comparisonPeriod) {
        super(expectedValue, ComparisonOperator.ABSOLUTE, comparisonPeriod);
    }

    @Override
    public boolean doVolatilityAssert(MetricsCollectedResult<String> currentValue, MetricsCollectedResult<String> benchmarkValue) {
        double expected = Double.parseDouble(getExpectedValue());
        double volatility = calculateVolatility(currentValue, benchmarkValue);
        return Math.abs(volatility) <= expected;
    }
}
