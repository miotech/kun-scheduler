package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ConnectionConfig {
    private final ConnectionInfo userConnection;
    private final ConnectionInfo dataConnection;
    private final ConnectionInfo metadataConnection;
    private final ConnectionInfo storageConnection;
    private Map<String,Object> values;

    @JsonCreator
    public ConnectionConfig(@JsonProperty("userConnection") ConnectionInfo userConnection,
                            @JsonProperty("dataConnection") ConnectionInfo dataConnection,
                            @JsonProperty("metadataConnection") ConnectionInfo metadataConnection,
                            @JsonProperty("storageConnection") ConnectionInfo storageConnection) {
        this.userConnection = userConnection;
        this.dataConnection = dataConnection;
        this.metadataConnection = metadataConnection;
        this.storageConnection = storageConnection;
    }

    public ConnectionInfo getUserConnection(){
        return userConnection;
    }

    public ConnectionInfo getDataConnection(){
        if(dataConnection == null){
            return getUserConnection();
        }
        return dataConnection;
    }

    public ConnectionInfo getMetadataConnection(){
        if(metadataConnection == null){
            return getDataConnection();
        }
        return metadataConnection;
    }

    public ConnectionInfo getStorageConnection(){
        if(storageConnection == null){
            return getMetadataConnection();
        }
        return storageConnection;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public static ConnectionConfigBuilder newBuilder(){
        return new ConnectionConfigBuilder();
    }

    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "userConnection=" + userConnection +
                ", dataConnection=" + dataConnection +
                ", metadataConnection=" + metadataConnection +
                ", storageConnection=" + storageConnection +
                '}';
    }

    public static final class ConnectionConfigBuilder {
        private ConnectionInfo userConnection;
        private ConnectionInfo dataConnection;
        private ConnectionInfo metadataConnection;
        private ConnectionInfo storageConnection;

        private ConnectionConfigBuilder() {
        }

        public ConnectionConfigBuilder withUserConnection(ConnectionInfo userConnection) {
            this.userConnection = userConnection;
            return this;
        }

        public ConnectionConfigBuilder withDataConnection(ConnectionInfo dataConnection) {
            this.dataConnection = dataConnection;
            return this;
        }

        public ConnectionConfigBuilder withMetadataConnection(ConnectionInfo metadataConnection) {
            this.metadataConnection = metadataConnection;
            return this;
        }

        public ConnectionConfigBuilder withStorageConnection(ConnectionInfo storageConnection) {
            this.storageConnection = storageConnection;
            return this;
        }

        public ConnectionConfig build() {
            return new ConnectionConfig(userConnection, dataConnection, metadataConnection, storageConnection);
        }
    }
}
