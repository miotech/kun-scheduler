package com.miotech.kun.metadata.core.model.connection;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ESConnectionConfigInfo extends ConnectionConfigInfo {

    private final String host;
    private final Integer port;
    private final String username;
    private final String password;

    @JsonCreator
    public ESConnectionConfigInfo(@JsonProperty("connectionType") ConnectionType connectionType,
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

    public ESConnectionConfigInfo(ConnectionType connectionType, String host, Integer port) {
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

    @Override
    public boolean sameDatasource(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ESConnectionConfigInfo that = (ESConnectionConfigInfo) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ESConnectionConfigInfo that = (ESConnectionConfigInfo) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), host, port, username, password);
    }

    @Override
    public String toString() {
        return "ESConnectionInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
