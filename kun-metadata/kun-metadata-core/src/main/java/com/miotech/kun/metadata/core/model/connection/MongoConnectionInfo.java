package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MongoConnectionInfo extends ConnectionInfo {

    private final String host;
    private final Integer port;
    private final String username;
    private final String password;

    @JsonCreator
    public MongoConnectionInfo(@JsonProperty("connectionType") ConnectionType connectionType,
                               @JsonProperty("host") String host,
                               @JsonProperty("port") Integer port,
                               @JsonProperty("user") String username,
                               @JsonProperty("password") String password) {
        super(connectionType);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public MongoConnectionInfo(ConnectionType connectionType, String host, Integer port) {
        this(connectionType, host, port, null, null);
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
