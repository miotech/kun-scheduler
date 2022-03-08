package com.miotech.kun.dataquality.core.assertion;

public abstract class VolatilityAssertion extends Assertion {

    private final ComparisonPeriod comparisonPeriod;

    public VolatilityAssertion(String expectedType, String expectedValue, ComparisonOperator comparisonOperator, ComparisonPeriod comparisonPeriod) {
        super(AssertionType.VOLATILITY, comparisonOperator, expectedType, expectedValue);
        this.comparisonPeriod = comparisonPeriod;
    }

    public ComparisonPeriod getComparisonPeriod() {
        return comparisonPeriod;
    }
}
