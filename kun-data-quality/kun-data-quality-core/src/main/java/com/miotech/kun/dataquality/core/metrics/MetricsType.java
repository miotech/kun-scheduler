package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MetricsType {

    SQL, NO_SQL;

    @JsonCreator
    public static MetricsType from(String name) {
        return valueOf(name);
    }

}
