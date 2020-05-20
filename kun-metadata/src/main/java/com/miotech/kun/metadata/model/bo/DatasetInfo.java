package com.miotech.kun.metadata.model.bo;

import com.miotech.kun.metadata.model.DataStore;

public class DatasetInfo {

    private String name;

    private Long databaseId;

    private DataStore dataStore;

    public DatasetInfo(Builder builder) {
        this.name = builder.name;
        this.databaseId = builder.databaseId;
        this.dataStore = builder.dataStore;
    }

    public static class Builder {
        private String name;

        private Long databaseId;

        private DataStore dataStore;

        public Builder() {
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDatabaseId(Long databaseId) {
            this.databaseId = databaseId;
            return this;
        }

        public Builder setDataStore(DataStore dataStore) {
            this.dataStore = dataStore;
            return this;
        }

        public DatasetInfo build() {
            return new DatasetInfo(this);
        }
    }

}
