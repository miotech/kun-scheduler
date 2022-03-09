package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.dataquality.core.metrics.SQLMetricsCollectedResult;

public abstract class VolatilityAssertion extends Assertion {

    private final ComparisonPeriod comparisonPeriod;

    public VolatilityAssertion(String expectedType, String expectedValue, ComparisonOperator comparisonOperator, ComparisonPeriod comparisonPeriod) {
        super(AssertionType.VOLATILITY, comparisonPeriod, comparisonOperator, expectedType, expectedValue);
        this.comparisonPeriod = comparisonPeriod;
    }

    public ComparisonPeriod getComparisonPeriod() {
        return comparisonPeriod;
    }

    public abstract boolean doVolatilityAssert(SQLMetricsCollectedResult currentValue, SQLMetricsCollectedResult benchmarkValue);

    @Override
    public boolean doAssert(AssertionSample assertionSample) {
        SQLMetricsCollectedResult currentValue= (SQLMetricsCollectedResult) assertionSample.getCurrentValue();
        SQLMetricsCollectedResult benchmarkValue = (SQLMetricsCollectedResult) assertionSample.getBenchmarkValue();
        if (benchmarkValue == null) {
            return true;
        }

        return doVolatilityAssert(currentValue, benchmarkValue);
    }

}
