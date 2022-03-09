package com.miotech.kun.dataquality.core.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.common.utils.JSONUtils;

public class SQLMetricsProp implements MetricsProp {

    private final String sql;

    private final String field;

    @JsonCreator
    public SQLMetricsProp(@JsonProperty("sql") String sql,
                          @JsonProperty("field") String field) {
        this.sql = sql;
        this.field = field;
    }

    @Override
    public String toJsonString() {
        return JSONUtils.toJsonString(this);
    }
}
