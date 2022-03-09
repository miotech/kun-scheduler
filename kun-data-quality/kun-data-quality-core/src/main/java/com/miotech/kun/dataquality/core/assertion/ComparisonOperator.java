package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ComparisonOperator {

    EQUALS("EQ", "=")
    , LESS_THAN("LT", "<")
    , LESS_THAN_OR_EQUALS("LE", "<=")
    , GREATER_THAN("GT", ">")
    , GREATER_THAN_OR_EQUALS("GE", ">=")
    , NOT_EQUALS("NE", "!=")
    , ABSOLUTE("ABS", "|x|")
    , RISE("R", "+")
    , FALL("F", "-");

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
