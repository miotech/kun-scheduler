package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClusterConnection {

    private final String metaStoreUrl;

    private final String metaStoreUsername;

    private final String metaStorePassword;

    private final String dataStoreUrl;

    private final String dataStoreUsername;

    private final String dataStorePassword;

    @JsonCreator
    public ClusterConnection(@JsonProperty("metaStoreUrl") String metaStoreUrl,
                             @JsonProperty("metaStoreUsername") String metaStoreUsername,
                             @JsonProperty("metaStorePassword") String metaStorePassword,
                             @JsonProperty("dataStoreUrl") String dataStoreUrl,
                             @JsonProperty("dataStoreUsername") String dataStoreUsername,
                             @JsonProperty("dataStorePassword") String dataStorePassword) {
        this.metaStoreUrl = metaStoreUrl;
        this.metaStoreUsername = metaStoreUsername;
        this.metaStorePassword = metaStorePassword;
        this.dataStoreUrl = dataStoreUrl;
        this.dataStoreUsername = dataStoreUsername;
        this.dataStorePassword = dataStorePassword;
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

    public String getDataStoreUrl() {
        return dataStoreUrl;
    }

    public String getDataStoreUsername() {
        return dataStoreUsername;
    }

    public String getDataStorePassword() {
        return dataStorePassword;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String metaStoreUrl;
        private String metaStoreUsername;
        private String metaStorePassword;
        private String dataStoreUrl;
        private String dataStoreUsername;
        private String dataStorePassword;

        private Builder() {
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

        public ClusterConnection build() {
            return new ClusterConnection(metaStoreUrl, metaStoreUsername, metaStorePassword, dataStoreUrl, dataStoreUsername, dataStorePassword);
        }
    }
}
