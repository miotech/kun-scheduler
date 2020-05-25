package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HiveCluster extends Cluster {

    private String dataStoreUrl;

    private String dataStoreUsername;

    private String dataStorePassword;

    private String metaStoreUrl;

    private String metaStoreUsername;

    private String metaStorePassword;

    public String getDataStoreUrl() {
        return dataStoreUrl;
    }

    public String getDataStoreUsername() {
        return dataStoreUsername;
    }

    public String getDataStorePassword() {
        return dataStorePassword;
    }

    public String getMetaStoreUrl() {
        return metaStoreUrl;
    }

    public String getMetaStoreUsername() {
        return metaStoreUsername;
    }

    public String getMetaStorePassword() {
        return metaStorePassword;
    }


    @JsonCreator
    public HiveCluster(@JsonProperty("clusterId") long clusterId,
                       @JsonProperty("dataStoreUrl") String dataStoreUrl,
                       @JsonProperty("dataStoreUsername") String dataStoreUsername,
                       @JsonProperty("dataStorePassword") String dataStorePassword,
                       @JsonProperty("metaStoreUrl") String metaStoreUrl,
                       @JsonProperty("metaStoreUsername") String metaStoreUsername,
                       @JsonProperty("metaStorePassword") String metaStorePassword) {
        super(clusterId);
        this.dataStoreUrl = dataStoreUrl;
        this.dataStoreUsername = dataStoreUsername;
        this.dataStorePassword = dataStorePassword;
        this.metaStoreUrl = metaStoreUrl;
        this.metaStoreUsername = metaStoreUsername;
        this.metaStorePassword = metaStorePassword;
    }

    public static HiveCluster.Builder newBuilder() {
        return new HiveCluster.Builder();
    }

    @Override
    public String toString() {
        return null;
    }

    public static final class Builder {
        private long clusterId;
        private String dataStoreUrl;
        private String dataStoreUsername;
        private String dataStorePassword;
        private String metaStoreUrl;
        private String metaStoreUsername;
        private String metaStorePassword;

        private Builder() {
        }

        public Builder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public Builder withDataStoreUrl(String dataStoreUrl) {
            this.dataStoreUrl = dataStoreUrl;
            return this;
        }

        public Builder withDataStoreUsername(String dataStoreUsername) {
            this.dataStoreUsername = dataStoreUsername;
            return this;
        }

        public Builder withDataStorePassword(String dataStorePassword) {
            this.dataStorePassword = dataStorePassword;
            return this;
        }

        public Builder withMetaStoreUrl(String metaStoreUrl) {
            this.metaStoreUrl = metaStoreUrl;
            return this;
        }

        public Builder withMetaStoreUsername(String metaStoreUsername) {
            this.metaStoreUsername = metaStoreUsername;
            return this;
        }

        public Builder withMetaStorePassword(String metaStorePassword) {
            this.metaStorePassword = metaStorePassword;
            return this;
        }

        public HiveCluster build() {
            return new HiveCluster(clusterId, dataStoreUrl, dataStoreUsername, dataStorePassword, metaStoreUrl, metaStoreUsername, metaStorePassword);
        }
    }
}
