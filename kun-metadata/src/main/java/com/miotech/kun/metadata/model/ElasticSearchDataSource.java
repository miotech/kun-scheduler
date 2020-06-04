package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchDataSource extends DataSource{
    final String dataStoreUrl;
    final String dataStoreUsername;
    final String dataStorePassword;


    @JsonCreator
    public ElasticSearchDataSource(@JsonProperty("clusterId") long clusterId,
                                @JsonProperty("dataStoreUrl") String dataStoreUrl,
                                @JsonProperty("dataStoreUsername") String dataStoreUsername,
                                @JsonProperty("dataStorePassword") String dataStorePassword) {
        super(clusterId, Type.ElasticSearch);
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
        return "ElasticSearchDataSource{" +
                ", dataStoreUrl='" + dataStoreUrl + '\'' +
                ", dataStoreUsername='" + dataStoreUsername + '\'' +
                ", dataStorePassword='" + dataStorePassword + '\'' +
                '}';
    }

    public static ElasticSearchDataSource.ElasticSearchDataSourceBuilder newBuilder() {
        return new ElasticSearchDataSource.ElasticSearchDataSourceBuilder();
    }


    public static final class ElasticSearchDataSourceBuilder {
        String dataStoreUrl;
        String dataStoreUsername;
        String dataStorePassword;
        private long clusterId;

        private ElasticSearchDataSourceBuilder() {
        }

        public static ElasticSearchDataSource.ElasticSearchDataSourceBuilder aElasticSearchDataSource() {
            return new ElasticSearchDataSource.ElasticSearchDataSourceBuilder();
        }

        public ElasticSearchDataSource.ElasticSearchDataSourceBuilder withDataStoreUrl(String dataStoreUrl) {
            this.dataStoreUrl = dataStoreUrl;
            return this;
        }

        public ElasticSearchDataSource.ElasticSearchDataSourceBuilder withDataStoreUsername(String dataStoreUsername) {
            this.dataStoreUsername = dataStoreUsername;
            return this;
        }

        public ElasticSearchDataSource.ElasticSearchDataSourceBuilder withDataStorePassword(String dataStorePassword) {
            this.dataStorePassword = dataStorePassword;
            return this;
        }

        public ElasticSearchDataSource.ElasticSearchDataSourceBuilder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public ElasticSearchDataSource build() {
            return new ElasticSearchDataSource(clusterId, dataStoreUrl, dataStoreUsername, dataStorePassword);
        }
    }
}
