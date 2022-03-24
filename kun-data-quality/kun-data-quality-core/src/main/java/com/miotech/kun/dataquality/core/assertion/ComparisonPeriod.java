package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;

public class ComparisonPeriod {

    private final int daysAgo;

    @JsonCreator
    public ComparisonPeriod(@JsonProperty("daysAgo") int daysAgo) {
        Preconditions.checkArgument(daysAgo >= 0, "`comparisonPeriod` must be greater than or equal to 0");
        this.daysAgo = daysAgo;
    }

    public ComparisonPeriod(FixedPeriod fixedPeriod) {
        this.daysAgo = fixedPeriod.getDaysAgo();
    }

    public static ComparisonPeriod from(FixedPeriod fixedPeriod) {
        return new ComparisonPeriod(fixedPeriod);
    }

    public int getDaysAgo() {
        return daysAgo;
    }

    public enum FixedPeriod {
        THIS_TIME(0)
        , LAST_TIME(1)
        , SEVEN_DAYS_AGO(7)
        , THIRTY_DAYS_AGO(30)
        ;

        private int daysAgo;

        FixedPeriod(int daysAgo) {
            this.daysAgo = daysAgo;
        }

        public int getDaysAgo() {
            return daysAgo;
        }

        @JsonCreator
        public static FixedPeriod forValue(int daysAgo) {
            for (FixedPeriod value : values()) {
                if (value.getDaysAgo() == daysAgo) {
                    return value;
                }
            }

            return null;
        }

        @JsonValue
        public String toValue() {
            return this.name();
        }

    }

}
