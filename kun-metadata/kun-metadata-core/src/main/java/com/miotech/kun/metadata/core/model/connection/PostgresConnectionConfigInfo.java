package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PostgresConnectionConfigInfo extends JdbcConnectionConfigInfo {

    private final String host;
    private final Integer port;
    private final String username;
    private final String password;

    @JsonCreator
    public PostgresConnectionConfigInfo(@JsonProperty("connectionType") ConnectionType connectionType,
                                        @JsonProperty("host") String host,
                                        @JsonProperty("port") Integer port,
                                        @JsonProperty("username") String username,
                                        @JsonProperty("password") String password) {
        super(connectionType);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public PostgresConnectionConfigInfo(ConnectionType connectionType, String host, Integer port) {
        this(connectionType, host, port, null, null);
    }

    @Override
    @JsonIgnore
    public String getJdbcUrl() {
        return ConnectUrlUtil.convertToConnectUrl(host, port, ConnectionType.POSTGRESQL);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PostgresConnectionConfigInfo that = (PostgresConnectionConfigInfo) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), host, port, username, password);
    }

    @Override
    public String toString() {
        return "PostgresConnectionInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
