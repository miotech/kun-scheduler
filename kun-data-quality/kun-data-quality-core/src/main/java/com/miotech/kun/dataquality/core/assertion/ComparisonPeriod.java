package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ComparisonPeriod {

    LAST_TIME
    , SEVEN_DAYS_AGO
    , THIRTY_DAYS_AGO
    ;


    @JsonCreator
    public static ComparisonPeriod forValue(String value) {
        return valueOf(value);
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }

}
