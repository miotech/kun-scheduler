package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchCluster extends Cluster{
    final String dataStoreUrl;
    final String dataStoreUsername;
    final String dataStorePassword;


    @JsonCreator
    public ElasticSearchCluster(@JsonProperty("clusterId") long clusterId,
                         @JsonProperty("dataStoreUrl") String dataStoreUrl,
                         @JsonProperty("dataStoreUsername") String dataStoreUsername,
                         @JsonProperty("dataStorePassword") String dataStorePassword) {
        super(clusterId);
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
        //TODO
        return null;
    }

    public static ElasticSearchCluster.ElasticSearchClusterBuilder newBuilder() {
        return new ElasticSearchCluster.ElasticSearchClusterBuilder();
    }


    public static final class ElasticSearchClusterBuilder {
        String dataStoreUrl;
        String dataStoreUsername;
        String dataStorePassword;
        private long clusterId;

        private ElasticSearchClusterBuilder() {
        }

        public static ElasticSearchCluster.ElasticSearchClusterBuilder aElasticSearchCluster() {
            return new ElasticSearchCluster.ElasticSearchClusterBuilder();
        }

        public ElasticSearchCluster.ElasticSearchClusterBuilder withDataStoreUrl(String dataStoreUrl) {
            this.dataStoreUrl = dataStoreUrl;
            return this;
        }

        public ElasticSearchCluster.ElasticSearchClusterBuilder withDataStoreUsername(String dataStoreUsername) {
            this.dataStoreUsername = dataStoreUsername;
            return this;
        }

        public ElasticSearchCluster.ElasticSearchClusterBuilder withDataStorePassword(String dataStorePassword) {
            this.dataStorePassword = dataStorePassword;
            return this;
        }

        public ElasticSearchCluster.ElasticSearchClusterBuilder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public ElasticSearchCluster build() {
            return new ElasticSearchCluster(clusterId, dataStoreUrl, dataStoreUsername, dataStorePassword);
        }
    }
}
