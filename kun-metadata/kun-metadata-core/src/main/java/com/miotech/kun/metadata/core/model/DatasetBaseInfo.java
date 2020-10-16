package com.miotech.kun.metadata.core.model;

import java.io.Serializable;

public class DatasetBaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long gid;

    private final Long datasourceId;

    private final String name;

    private final DataStore dataStore;

    private final String databaseName;

    public DatasetBaseInfo(Long gid, Long datasourceId, String name, DataStore dataStore, String databaseName) {
        this.gid = gid;
        this.datasourceId = datasourceId;
        this.name = name;
        this.dataStore = dataStore;
        this.databaseName = databaseName;
    }

    public Long getGid() {
        return gid;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public String getName() {
        return name;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long gid;
        private Long datasourceId;
        private String name;
        private DataStore dataStore;
        private String databaseName;

        private Builder() {
        }

        public Builder withGid(Long gid) {
            this.gid = gid;
            return this;
        }

        public Builder withDatasourceId(Long datasourceId) {
            this.datasourceId = datasourceId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDataStore(DataStore dataStore) {
            this.dataStore = dataStore;
            return this;
        }

        public Builder withDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public DatasetBaseInfo build() {
            return new DatasetBaseInfo(gid, datasourceId, name, dataStore, databaseName);
        }
    }
}
