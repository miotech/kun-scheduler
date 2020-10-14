package com.miotech.kun.metadata.core.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

@JsonDeserialize(builder = DataSourceConnectionDTO.Builder.class)
public class DataSourceConnectionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String host;

    private final String port;

    private final String username;

    private final String password;

    public DataSourceConnectionDTO(String host, String port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String host;
        private String port;
        private String username;
        private String password;

        private Builder() {
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(String port) {
            this.port = port;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public DataSourceConnectionDTO build() {
            return new DataSourceConnectionDTO(host, port, username, password);
        }
    }
}
