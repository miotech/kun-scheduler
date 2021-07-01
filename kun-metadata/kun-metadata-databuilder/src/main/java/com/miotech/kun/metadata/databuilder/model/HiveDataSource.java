package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.tool.ConnectUrlUtil;

public class HiveDataSource extends DataSource {

    private final String metaStoreUris;

    private final String dataStoreHost;

    private final int dataStorePort;

    private final String dataStoreUsername;

    private final String dataStorePassword;

    @JsonCreator
    public HiveDataSource(@JsonProperty("id") long id,
                          @JsonProperty("metaStoreUris") String metaStoreUris,
                          @JsonProperty("dataStoreHost") String dataStoreHost,
                          @JsonProperty("dataStorePort") int dataStorePort,
                          @JsonProperty("dataStoreUsername") String dataStoreUsername,
                          @JsonProperty("dataStorePassword") String dataStorePassword) {
        super(id, Type.HIVE);
        this.metaStoreUris = metaStoreUris;
        this.dataStoreHost = dataStoreHost;
        this.dataStorePort = dataStorePort;
        this.dataStoreUsername = dataStoreUsername;
        this.dataStorePassword = dataStorePassword;
    }

    public String getMetaStoreUris() {
        return metaStoreUris;
    }

    public String getDataStoreUsername() {
        return dataStoreUsername;
    }

    public String getDataStorePassword() {
        return dataStorePassword;
    }

    public String getDataStoreUrl() {
        return ConnectUrlUtil.convertToConnectUrl(dataStoreHost, dataStorePort, dataStoreUsername, dataStorePassword, DatabaseType.HIVE);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withMetaStoreUris(metaStoreUris)
                .withDataStoreHost(dataStoreHost)
                .withDataStorePort(dataStorePort)
                .withDataStoreUsername(dataStoreUsername)
                .withDataStorePassword(dataStorePassword);
    }


    public static final class Builder {
        private String metaStoreUris;
        private String dataStoreHost;
        private int dataStorePort;
        private String dataStoreUsername;
        private String dataStorePassword;
        private long id;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withMetaStoreUris(String metaStoreUris) {
            this.metaStoreUris = metaStoreUris;
            return this;
        }

        public Builder withDataStoreHost(String dataStoreHost) {
            this.dataStoreHost = dataStoreHost;
            return this;
        }

        public Builder withDataStorePort(int dataStorePort) {
            this.dataStorePort = dataStorePort;
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

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public HiveDataSource build() {
            return new HiveDataSource(id, metaStoreUris, dataStoreHost, dataStorePort, dataStoreUsername, dataStorePassword);
        }
    }
}
