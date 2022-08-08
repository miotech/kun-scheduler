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
        double current = Double.parseDouble(currentValue.getValue());
        double baseline = Double.parseDouble(benchmarkValue.getValue());
        double volatility = ((current - baseline) / baseline) * 100;
        return Math.abs(volatility) <= expected;
    }
}
