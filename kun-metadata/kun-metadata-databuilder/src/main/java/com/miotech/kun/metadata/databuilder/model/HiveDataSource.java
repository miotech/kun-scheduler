package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HiveDataSource extends DataSource {

    private final String metastoreUrl;

    private final String metastoreUsername;

    private final String metastorePassword;

    private final String datastoreUrl;

    private final String datastoreUsername;

    private final String datastorePassword;

    @JsonCreator
    public HiveDataSource(@JsonProperty("id") long id,
                          @JsonProperty("metastoreUrl") String metastoreUrl,
                          @JsonProperty("metastoreUsername") String metastoreUsername,
                          @JsonProperty("metastorePassword") String metastorePassword,
                          @JsonProperty("datastoreUrl") String datastoreUrl,
                          @JsonProperty("datastoreUsername") String datastoreUsername,
                          @JsonProperty("datastorePassword") String datastorePassword) {
        super(id, Type.HIVE);
        this.metastoreUrl = metastoreUrl;
        this.metastoreUsername = metastoreUsername;
        this.metastorePassword = metastorePassword;
        this.datastoreUrl = datastoreUrl;
        this.datastoreUsername = datastoreUsername;
        this.datastorePassword = datastorePassword;
    }

    public String getMetastoreUrl() {
        return metastoreUrl;
    }

    public String getMetastoreUsername() {
        return metastoreUsername;
    }

    public String getMetastorePassword() {
        return metastorePassword;
    }

    public String getDatastoreUrl() {
        return datastoreUrl;
    }

    public String getDatastoreUsername() {
        return datastoreUsername;
    }

    public String getDatastorePassword() {
        return datastorePassword;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder().withMetastoreUrl(metastoreUrl)
                .withMetastoreUsername(metastoreUsername)
                .withMetastorePassword(metastorePassword)
                .withDatastoreUrl(datastoreUrl)
                .withDatastoreUsername(datastoreUsername)
                .withDatastorePassword(datastorePassword);
    }

    public static final class Builder {
        private String metastoreUrl;
        private String metastoreUsername;
        private String metastorePassword;
        private String datastoreUrl;
        private String datastoreUsername;
        private String datastorePassword;
        private long id;

        private Builder() {
        }

        public Builder withMetastoreUrl(String metastoreUrl) {
            this.metastoreUrl = metastoreUrl;
            return this;
        }

        public Builder withMetastoreUsername(String metastoreUsername) {
            this.metastoreUsername = metastoreUsername;
            return this;
        }

        public Builder withMetastorePassword(String metastorePassword) {
            this.metastorePassword = metastorePassword;
            return this;
        }

        public Builder withDatastoreUrl(String datastoreUrl) {
            this.datastoreUrl = datastoreUrl;
            return this;
        }

        public Builder withDatastoreUsername(String datastoreUsername) {
            this.datastoreUsername = datastoreUsername;
            return this;
        }

        public Builder withDatastorePassword(String datastorePassword) {
            this.datastorePassword = datastorePassword;
            return this;
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public HiveDataSource build() {
            return new HiveDataSource(id, metastoreUrl, metastoreUsername, metastorePassword, datastoreUrl, datastoreUsername, datastorePassword);
        }
    }
}
