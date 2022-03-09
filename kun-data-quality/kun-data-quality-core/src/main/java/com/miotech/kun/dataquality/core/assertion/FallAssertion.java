package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.SQLMetricsCollectedResult;

public class FallAssertion extends VolatilityAssertion {

    @JsonCreator
    public FallAssertion(@JsonProperty("expectedType") String expectedType,
                         @JsonProperty("expectedValue") String expectedValue,
                         @JsonProperty("comparisonPeriod") ComparisonPeriod comparisonPeriod) {
        super(expectedType, expectedValue, ComparisonOperator.FALL, comparisonPeriod);
    }


    @Override
    public boolean doVolatilityAssert(SQLMetricsCollectedResult currentValue, SQLMetricsCollectedResult benchmarkValue) {
        double expected = Double.parseDouble(getExpectedValue());
        double current = Double.parseDouble(currentValue.getResult());
        double baseline = Double.parseDouble(benchmarkValue.getResult());

        double volatility = ((current - baseline) / baseline) * 100;
        return volatility <= 0 && volatility <= expected;
    }
}
