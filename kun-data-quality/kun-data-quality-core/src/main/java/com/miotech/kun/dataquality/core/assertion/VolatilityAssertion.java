package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public abstract class VolatilityAssertion extends Assertion {

    private final ComparisonPeriod comparisonPeriod;

    public VolatilityAssertion(String expectedValue, ComparisonOperator comparisonOperator, ComparisonPeriod comparisonPeriod) {
        super(AssertionType.VOLATILITY, comparisonPeriod, comparisonOperator, expectedValue);
        this.comparisonPeriod = comparisonPeriod;
    }

    public ComparisonPeriod getComparisonPeriod() {
        return comparisonPeriod;
    }

    public abstract boolean doVolatilityAssert(MetricsCollectedResult<String> currentValue, MetricsCollectedResult<String> benchmarkValue);

    public Double calculateVolatility(MetricsCollectedResult<String> currentMetricsCollectedResult, MetricsCollectedResult<String> benchmarkMetricsCollectedNResult) {
        double current = Double.parseDouble(currentMetricsCollectedResult.getValue());
        double baseline = Double.parseDouble(benchmarkMetricsCollectedNResult.getValue());
        return ((current - baseline) / baseline) * 100;
    }

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
