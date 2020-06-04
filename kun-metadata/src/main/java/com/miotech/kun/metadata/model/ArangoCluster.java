package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArangoCluster extends Cluster{
    final String dataStoreUrl;
    final String dataStoreUsername;
    final String dataStorePassword;


    @JsonCreator
    public ArangoCluster(@JsonProperty("clusterId") long clusterId,
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

    public static ArangoCluster.ArangoClusterBuilder newBuilder() {
        return new ArangoCluster.ArangoClusterBuilder();
    }


    public static final class ArangoClusterBuilder {
        String dataStoreUrl;
        String dataStoreUsername;
        String dataStorePassword;
        private long clusterId;

        private ArangoClusterBuilder() {
        }

        public static ArangoCluster.ArangoClusterBuilder ArangoCluster() {
            return new ArangoCluster.ArangoClusterBuilder();
        }

        public ArangoCluster.ArangoClusterBuilder withDataStoreUrl(String dataStoreUrl) {
            this.dataStoreUrl = dataStoreUrl;
            return this;
        }

        public ArangoCluster.ArangoClusterBuilder withDataStoreUsername(String dataStoreUsername) {
            this.dataStoreUsername = dataStoreUsername;
            return this;
        }

        public ArangoCluster.ArangoClusterBuilder withDataStorePassword(String dataStorePassword) {
            this.dataStorePassword = dataStorePassword;
            return this;
        }

        public ArangoCluster.ArangoClusterBuilder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public ArangoCluster build() {
            return new ArangoCluster(clusterId, dataStoreUrl, dataStoreUsername, dataStorePassword);
        }
    }
}
