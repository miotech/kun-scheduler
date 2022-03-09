package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public abstract class VolatilityAssertion extends Assertion {

    private final ComparisonPeriod comparisonPeriod;

    public VolatilityAssertion(String expectedType, String expectedValue, ComparisonOperator comparisonOperator, ComparisonPeriod comparisonPeriod) {
        super(AssertionType.VOLATILITY, comparisonPeriod, comparisonOperator, expectedType, expectedValue);
        this.comparisonPeriod = comparisonPeriod;
    }

    public ComparisonPeriod getComparisonPeriod() {
        return comparisonPeriod;
    }

    public abstract boolean doVolatilityAssert(MetricsCollectedResult<String> currentValue, MetricsCollectedResult<String> benchmarkValue);

    @Override
    public boolean doAssert(AssertionSample assertionSample) {
        MetricsCollectedResult<String> currentValue= (MetricsCollectedResult<String>) assertionSample.getCurrentValue();
        MetricsCollectedResult<String> benchmarkValue = (MetricsCollectedResult<String>) assertionSample.getBenchmarkValue();
        if (benchmarkValue == null) {
            return true;
        }

        return doVolatilityAssert(currentValue, benchmarkValue);
    }

}
