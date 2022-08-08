package com.miotech.kun.dataquality.core.metrics;

import com.miotech.kun.metadata.core.model.datasource.DataSource;

public class CollectContext {

    private final DataSource dataSource;

    public CollectContext(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
