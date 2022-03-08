package com.miotech.kun.dataquality.core.expectation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.assertion.ComparisonOperator;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetricsCollectedResult;

public class AssertionResult {

    private final String field;

    private final ComparisonOperator comparisonOperator;

    private final String operator;

    private final String expectedType;

    private final String expectedValue;

    private final String originalValue;

    @JsonCreator
    public AssertionResult(@JsonProperty("field") String field, @JsonProperty("comparisonOperator") ComparisonOperator comparisonOperator,
                           @JsonProperty("operator") String operator, @JsonProperty("expectedType") String expectedType,
                           @JsonProperty("expectedValue") String expectedValue, @JsonProperty("originalValue") String originalValue) {
        this.field = field;
        this.comparisonOperator = comparisonOperator;
        this.operator = operator;
        this.expectedType = expectedType;
        this.expectedValue = expectedValue;
        this.originalValue = originalValue;
    }

    public String getField() {
        return field;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public String getOperator() {
        return operator;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public static AssertionResult from(SQLMetrics sqlMetrics, Assertion assertion, SQLMetricsCollectedResult sqlMetricsCollectedResult) {
        return AssertionResult.newBuilder()
                .withField(sqlMetrics.getField())
                .withComparisonOperator(assertion.getComparisonOperator())
                .withOperator(assertion.getComparisonOperator().getSymbol())
//                .withExpectedType(assertion.getExpectedType())
                .withExpectedValue(assertion.getExpectedValue())
                .withOriginalValue(sqlMetricsCollectedResult.getValue())
                .build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String field;
        private ComparisonOperator comparisonOperator;
        private String operator;
        private String expectedType;
        private String expectedValue;
        private String originalValue;

        private Builder() {
        }

        public Builder withField(String field) {
            this.field = field;
            return this;
        }

        public Builder withComparisonOperator(ComparisonOperator comparisonOperator) {
            this.comparisonOperator = comparisonOperator;
            return this;
        }

        public Builder withOperator(String operator) {
            this.operator = operator;
            return this;
        }

        public Builder withExpectedType(String expectedType) {
            this.expectedType = expectedType;
            return this;
        }

        public Builder withExpectedValue(String expectedValue) {
            this.expectedValue = expectedValue;
            return this;
        }

        public Builder withOriginalValue(String originalValue) {
            this.originalValue = originalValue;
            return this;
        }

        public AssertionResult build() {
            return new AssertionResult(field, comparisonOperator, operator, expectedType, expectedValue, originalValue);
        }
    }
}
