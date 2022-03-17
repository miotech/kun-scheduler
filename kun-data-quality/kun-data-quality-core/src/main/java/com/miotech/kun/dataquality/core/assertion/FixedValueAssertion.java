package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public abstract class FixedValueAssertion extends Assertion {

    public FixedValueAssertion(String expectedType,
                               String expectedValue,
                               ComparisonOperator comparisonOperator) {
        super(AssertionType.FIXED_VALUE, ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME), comparisonOperator, expectedType, expectedValue);
    }

    public abstract boolean doFixValueAssert(MetricsCollectedResult<String> metricsCollectedResult);

    @Override
    public boolean doAssert(AssertionSample assertionSample) {
        return doFixValueAssert((MetricsCollectedResult<String>) assertionSample.getCurrentValue());
    }
}
