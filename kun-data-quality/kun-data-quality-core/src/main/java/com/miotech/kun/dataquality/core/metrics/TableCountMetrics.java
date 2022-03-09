package com.miotech.kun.dataquality.core.metrics;

import com.miotech.kun.dataquality.core.expectation.Dataset;

public class TableCountMetrics extends SQLMetrics {

    private final String baseSql = "SELECT COUNT(1) c FROM ${table_name}";
    private final String field = "c";

    public TableCountMetrics(String name, String description, Granularity granularity, Dataset dataset, String sql, String field) {
        super(name, description, granularity, dataset, sql, field);
    }

    public String getBaseSql() {
        return baseSql;
    }

    public String getField() {
        return field;
    }
}
