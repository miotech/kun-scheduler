package com.miotech.kun.metadata.model;

public class DatasetFieldRequest {

    private String table;

    private String metaStoreUrl;

    private String metaStoreUsername;

    private String metaStorePassword;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getMetaStoreUrl() {
        return metaStoreUrl;
    }

    public void setMetaStoreUrl(String metaStoreUrl) {
        this.metaStoreUrl = metaStoreUrl;
    }

    public String getMetaStoreUsername() {
        return metaStoreUsername;
    }

    public void setMetaStoreUsername(String metaStoreUsername) {
        this.metaStoreUsername = metaStoreUsername;
    }

    public String getMetaStorePassword() {
        return metaStorePassword;
    }

    public void setMetaStorePassword(String metaStorePassword) {
        this.metaStorePassword = metaStorePassword;
    }


    public static final class Builder {
        private String table;
        private String metaStoreUrl;
        private String metaStoreUsername;
        private String metaStorePassword;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder setTable(String table) {
            this.table = table;
            return this;
        }

        public Builder setMetaStoreUrl(String metaStoreUrl) {
            this.metaStoreUrl = metaStoreUrl;
            return this;
        }

        public Builder setMetaStoreUsername(String metaStoreUsername) {
            this.metaStoreUsername = metaStoreUsername;
            return this;
        }

        public Builder setMetaStorePassword(String metaStorePassword) {
            this.metaStorePassword = metaStorePassword;
            return this;
        }

        public DatasetFieldRequest build() {
            DatasetFieldRequest datasetFieldRequest = new DatasetFieldRequest();
            datasetFieldRequest.setTable(table);
            datasetFieldRequest.setMetaStoreUrl(metaStoreUrl);
            datasetFieldRequest.setMetaStoreUsername(metaStoreUsername);
            datasetFieldRequest.setMetaStorePassword(metaStorePassword);
            return datasetFieldRequest;
        }
    }
}
