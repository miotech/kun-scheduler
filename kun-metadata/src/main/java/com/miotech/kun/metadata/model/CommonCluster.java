package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommonCluster extends Cluster{

    final String dataStoreUrl;
    final String dataStoreUsername;
    final String dataStorePassword;


    @JsonCreator
    public CommonCluster(@JsonProperty("clusterId") long clusterId,
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

    public static CommonClusterBuilder newBuilder() {
        return new CommonClusterBuilder();
    }


    public static final class CommonClusterBuilder {
        String dataStoreUrl;
        String dataStoreUsername;
        String dataStorePassword;
        private long clusterId;

        private CommonClusterBuilder() {
        }

        public static CommonClusterBuilder aCommonCluster() {
            return new CommonClusterBuilder();
        }

        public CommonClusterBuilder withDataStoreUrl(String dataStoreUrl) {
            this.dataStoreUrl = dataStoreUrl;
            return this;
        }

        public CommonClusterBuilder withDataStoreUsername(String dataStoreUsername) {
            this.dataStoreUsername = dataStoreUsername;
            return this;
        }

        public CommonClusterBuilder withDataStorePassword(String dataStorePassword) {
            this.dataStorePassword = dataStorePassword;
            return this;
        }

        public CommonClusterBuilder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public CommonCluster build() {
            return new CommonCluster(clusterId, dataStoreUrl, dataStoreUsername, dataStorePassword);
        }
    }
}
