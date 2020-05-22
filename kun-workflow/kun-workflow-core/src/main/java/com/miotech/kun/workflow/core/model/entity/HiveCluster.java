package com.miotech.kun.workflow.core.model.entity;

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

    public static HiveCluster.Builder newBuilder() {
        return new HiveCluster.Builder();
    }

    @Override
    public String toString() {
        return null;
    }

    public static final class Builder {
        private String dataStoreUrl;
        private String dataStoreUsername;
        private String dataStorePassword;
        private String metaStoreUrl;
        private String metaStoreUsername;
        private String metaStorePassword;

        private Builder() {
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
            HiveCluster hiveCluster = new HiveCluster();
            hiveCluster.metaStoreUrl = this.metaStoreUrl;
            hiveCluster.dataStoreUrl = this.dataStoreUrl;
            hiveCluster.metaStoreUsername = this.metaStoreUsername;
            hiveCluster.metaStorePassword = this.metaStorePassword;
            hiveCluster.dataStoreUsername = this.dataStoreUsername;
            hiveCluster.dataStorePassword = this.dataStorePassword;
            return hiveCluster;
        }
    }
}
