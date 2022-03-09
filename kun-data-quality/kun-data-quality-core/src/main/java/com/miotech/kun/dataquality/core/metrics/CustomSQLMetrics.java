package com.miotech.kun.dataquality.core.metrics;

import com.miotech.kun.dataquality.core.expectation.Dataset;

public class CustomSQLMetrics extends SQLMetrics {

    public CustomSQLMetrics(String name, String description, Granularity granularity, Dataset dataset, String sql, String field) {
        super(name, description, granularity, dataset, sql, field);
    }

}
