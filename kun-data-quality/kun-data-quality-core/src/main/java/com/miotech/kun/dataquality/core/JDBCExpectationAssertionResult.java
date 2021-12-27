package com.miotech.kun.dataquality.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JDBCExpectationAssertionResult extends JDBCExpectationAssertion {

    private final String originalValue;

    @JsonCreator
    public JDBCExpectationAssertionResult(@JsonProperty("field") String field,
                                          @JsonProperty("comparisonOperator") ComparisonOperator comparisonOperator,
                                          @JsonProperty("operator") String operator,
                                          @JsonProperty("expectedType") String expectedType,
                                          @JsonProperty("expectedValue") String expectedValue,
                                          @JsonProperty("originalValue") String originalValue) {
        super(field, comparisonOperator, operator, expectedType, expectedValue);
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public static JDBCExpectationAssertionResult from(JDBCExpectationAssertion assertion, String originalValue) {
        return new JDBCExpectationAssertionResult(assertion.getField(), assertion.getComparisonOperator(), assertion.getComparisonOperator().getSymbol(),
                assertion.getExpectedType(), assertion.getExpectedValue(), originalValue);
    }

}