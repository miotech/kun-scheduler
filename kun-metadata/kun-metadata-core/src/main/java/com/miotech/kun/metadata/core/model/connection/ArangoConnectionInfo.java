package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ArangoConnectionInfo extends ConnectionInfo{

    private final String host;
    private final Integer port;
    private String username;
    private String password;

    @JsonCreator
    public ArangoConnectionInfo(@JsonProperty("connectionType") ConnectionType connectionType,
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

    public ArangoConnectionInfo(ConnectionType connectionType, String host, Integer port){
        super(connectionType);
        this.host = host;
        this.port = port;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ArangoConnectionInfo that = (ArangoConnectionInfo) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), host, port);
    }

    @Override
    public String toString() {
        return "ArangoConnectionInfo{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", userName='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
