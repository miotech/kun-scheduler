package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.dataquality.core.metrics.SQLMetricsCollectedResult;

public abstract class FixedValueAssertion extends Assertion {

    public FixedValueAssertion(String expectedType,
                               String expectedValue,
                               ComparisonOperator comparisonOperator) {
        super(AssertionType.FIXED_VALUE, ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME), comparisonOperator, expectedType, expectedValue);
    }

    public abstract boolean doFixValueAssert(SQLMetricsCollectedResult sqlMetricsCollectedResult);

    @Override
    public boolean doAssert(AssertionSample assertionSample) {
        return doFixValueAssert((SQLMetricsCollectedResult) assertionSample.getCurrentValue());
    }
}
