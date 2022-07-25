package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property = "comparisonOperator", visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = EqualsAssertion.class, name = "EQUALS"),
        @JsonSubTypes.Type(value = LessThanAssertion.class, name = "LESS_THAN"),
        @JsonSubTypes.Type(value = LessThanOrEqualsAssertion.class, name = "LESS_THAN_OR_EQUALS"),
        @JsonSubTypes.Type(value = GreaterThanAssertion.class, name = "GREATER_THAN"),
        @JsonSubTypes.Type(value = GreaterThanOrEqualsAssertion.class, name = "GREATER_THAN_OR_EQUALS"),
        @JsonSubTypes.Type(value = NotEqualsAssertion.class, name = "NOT_EQUALS"),
        @JsonSubTypes.Type(value = AbsoluteAssertion.class, name = "ABSOLUTE"),
        @JsonSubTypes.Type(value = RiseAssertion.class, name = "RISE"),
        @JsonSubTypes.Type(value = FallAssertion.class, name = "FALL")
})
public abstract class Assertion {

    private final AssertionType assertionType;

    private final ComparisonPeriod comparisonPeriod;

    private final ComparisonOperator comparisonOperator;

    private final String expectedValue;

    public Assertion(AssertionType assertionType,
                     ComparisonPeriod comparisonPeriod,
                     ComparisonOperator comparisonOperator,
                     String expectedValue) {
        this.assertionType = assertionType;
        this.comparisonPeriod = comparisonPeriod;
        this.comparisonOperator = comparisonOperator;
        this.expectedValue = expectedValue;
    }

    public AssertionType getAssertionType() {
        return assertionType;
    }

    public ComparisonPeriod getComparisonPeriod() {
        return comparisonPeriod;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public abstract boolean doAssert(AssertionSample assertionSample);

}
