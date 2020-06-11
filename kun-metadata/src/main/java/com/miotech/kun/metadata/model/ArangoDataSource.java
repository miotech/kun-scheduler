package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArangoDataSource extends DataSource{
    final String dataStoreUrl;
    final String dataStoreUsername;
    final String dataStorePassword;


    @JsonCreator
    public ArangoDataSource(@JsonProperty("clusterId") long clusterId,
                         @JsonProperty("dataStoreUrl") String dataStoreUrl,
                         @JsonProperty("dataStoreUsername") String dataStoreUsername,
                         @JsonProperty("dataStorePassword") String dataStorePassword) {
        super(clusterId, Type.Arango);
        this.dataStoreUrl = dataStoreUrl;
        this.dataStoreUsername = dataStoreUsername;
        this.dataStorePassword = dataStorePassword;
    }

    public String getDataStoreUrl() {
        return dataStoreUrl;
    }

    public String getDataStoreUsername() {
        return dataStoreUsername;
    }

    public String getDataStorePassword() {
        return dataStorePassword;
    }

    @Override
    public String toString() {
        return "ArangoDataSource{" +
                ", dataStoreUrl='" + dataStoreUrl + '\'' +
                ", dataStoreUsername='" + dataStoreUsername + '\'' +
                ", dataStorePassword='" + dataStorePassword + '\'' +
                '}';
    }

    public static ArangoDataSource.ArangoDataSourceBuilder newBuilder() {
        return new ArangoDataSource.ArangoDataSourceBuilder();
    }


    public static final class ArangoDataSourceBuilder {
        String dataStoreUrl;
        String dataStoreUsername;
        String dataStorePassword;
        private long clusterId;

        private ArangoDataSourceBuilder() {
        }

        public static ArangoDataSource.ArangoDataSourceBuilder ArangoDataSource() {
            return new ArangoDataSource.ArangoDataSourceBuilder();
        }

        public ArangoDataSource.ArangoDataSourceBuilder withDataStoreUrl(String dataStoreUrl) {
            this.dataStoreUrl = dataStoreUrl;
            return this;
        }

        public ArangoDataSource.ArangoDataSourceBuilder withDataStoreUsername(String dataStoreUsername) {
            this.dataStoreUsername = dataStoreUsername;
            return this;
        }

        public ArangoDataSource.ArangoDataSourceBuilder withDataStorePassword(String dataStorePassword) {
            this.dataStorePassword = dataStorePassword;
            return this;
        }

        public ArangoDataSource.ArangoDataSourceBuilder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public ArangoDataSource build() {
            return new ArangoDataSource(clusterId, dataStoreUrl, dataStoreUsername, dataStorePassword);
        }
    }
}
