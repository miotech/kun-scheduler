package com.miotech.kun.metadata.databuilder.model;

import com.miotech.kun.workflow.core.model.lineage.DataStore;

public class DatasetConnDto {

    private final long gid;

    private final DataStore dataStore;

    private final DataSource dataSource;

    public DatasetConnDto(long gid, DataStore dataStore, DataSource dataSource) {
        this.gid = gid;
        this.dataStore = dataStore;
        this.dataSource = dataSource;
    }

    public long getGid() {
        return gid;
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
        private long gid;
        private DataStore dataStore;
        private DataSource dataSource;

        private Builder() {
        }

        public Builder withGid(long gid) {
            this.gid = gid;
            return this;
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
            return new DatasetConnDto(gid, dataStore, dataSource);
        }
    }
}
