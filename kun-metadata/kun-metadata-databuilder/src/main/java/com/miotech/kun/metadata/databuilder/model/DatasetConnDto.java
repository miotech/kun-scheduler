package com.miotech.kun.metadata.databuilder.model;

import com.miotech.kun.workflow.core.model.lineage.DataStore;

public class DatasetConnDto {

    private final DataStore dataStore;

    private final DataSource dataSource;

    public DatasetConnDto(DataStore dataStore, DataSource dataSource) {
        this.dataStore = dataStore;
        this.dataSource = dataSource;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private DataStore dataStore;
        private DataSource dataSource;

        private Builder() {
        }

        public Builder withDataStore(DataStore dataStore) {
            this.dataStore = dataStore;
            return this;
        }

        public Builder withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public DatasetConnDto build() {
            return new DatasetConnDto(dataStore, dataSource);
        }
    }
}
