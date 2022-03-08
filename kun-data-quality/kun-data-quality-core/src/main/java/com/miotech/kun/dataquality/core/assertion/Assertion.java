package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property = "comparisonOperator", visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = EqualsAssertion.class, name = "EQUALS"),
        @JsonSubTypes.Type(value = LessThanAssertion.class, name = "LESS_THAN"),
        @JsonSubTypes.Type(value = LessThanOrEqualsAssertion.class, name = "LESS_THAN_OR_EQUALS"),
        @JsonSubTypes.Type(value = GreaterThanAssertion.class, name = "GREATER_THAN"),
        @JsonSubTypes.Type(value = GreaterThanOrEqualsAssertion.class, name = "GREATER_THAN_OR_EQUALS"),
        @JsonSubTypes.Type(value = NotEqualsAssertion.class, name = "NOT_EQUALS"),
        @JsonSubTypes.Type(value = AbsoluteAssertion.class, name = "ABSOLUTE")
})
public abstract class Assertion {

    private final AssertionType assertionType;

    private final ComparisonOperator comparisonOperator;

    private final String expectedType;

    private final String expectedValue;

    public Assertion(AssertionType assertionType,
                     ComparisonOperator comparisonOperator,
                     String expectedType,
                     String expectedValue) {
        this.assertionType = assertionType;
        this.comparisonOperator = comparisonOperator;
        this.expectedType = expectedType;
        this.expectedValue = expectedValue;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public abstract boolean doAssert(MetricsCollectedResult metricsCollectedResult);

}
