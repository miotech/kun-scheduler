package com.miotech.kun.dataquality.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class JDBCExpectationAssertion {

    private final String field;

    private final ComparisonOperator comparisonOperator;

    private final String operator;

    private final String expectedType;

    private final String expectedValue;

    @JsonCreator
    public JDBCExpectationAssertion(@JsonProperty("field") String field,
                                    @JsonProperty("comparisonOperator") ComparisonOperator comparisonOperator,
                                    @JsonProperty("operator") String operator,
                                    @JsonProperty("expectedType") String expectedType,
                                    @JsonProperty("expectedValue") String expectedValue) {
        Preconditions.checkArgument(StringUtils.isNotBlank(field));
        Preconditions.checkNotNull(comparisonOperator);
        Preconditions.checkArgument(StringUtils.isNotBlank(expectedValue));
        this.field = field;
        this.comparisonOperator = comparisonOperator;
        this.operator = operator;
        this.expectedType = expectedType;
        this.expectedValue = expectedValue;
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

    public boolean doAssert(String originalValue) {
        switch (comparisonOperator) {
            case EQUALS:
                if (expectedValue.contains(".") || originalValue.contains(".")) {
                    return Double.parseDouble(expectedValue) == Double.parseDouble(originalValue);
                }
                return expectedValue.equals(originalValue);
            case LESS_THAN:
                return Double.parseDouble(originalValue) < Double.parseDouble(expectedValue);
            case LESS_THAN_OR_EQUALS:
                return Double.parseDouble(originalValue) <= Double.parseDouble(expectedValue);
            case GREATER_THAN:
                return Double.parseDouble(originalValue) > Double.parseDouble(expectedValue);
            case GREATER_THAN_OR_EQUALS:
                return Double.parseDouble(originalValue) >= Double.parseDouble(expectedValue);
            case NOT_EQUALS:
                return Double.parseDouble(originalValue) != Double.parseDouble(expectedValue);
            default:
                throw new IllegalArgumentException("Invalid comparisonOperator: " + comparisonOperator);
        }
    }

    public enum ComparisonOperator {
        EQUALS("EQ", "=")                    // =
        , LESS_THAN("LT", "<")               // <
        , LESS_THAN_OR_EQUALS("LE", "<=")     // <=
        , GREATER_THAN("GT", ">")            // >
        , GREATER_THAN_OR_EQUALS("GE", ">=")  // >=
        , NOT_EQUALS("NE", "!=")              // !=
        ;

        private final String abbreviation;

        private final String symbol;

        ComparisonOperator(String abbreviation, String symbol) {
            this.abbreviation = abbreviation;
            this.symbol = symbol;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public String getSymbol() {
            return symbol;
        }

        @JsonCreator
        public static ComparisonOperator forValue(String value) {
            return valueOf(value);
        }

        public static ComparisonOperator convertFrom(String symbol) {
            for (ComparisonOperator value : values()) {
                if (value.getSymbol().equals(symbol)) {
                    return value;
                }
            }

            throw new IllegalStateException(String.format("Cannot convert symbol: %s to ComparisonOperator", symbol));
        }

        @JsonValue
        public String toValue() {
            return this.name();
        }
    }

}
