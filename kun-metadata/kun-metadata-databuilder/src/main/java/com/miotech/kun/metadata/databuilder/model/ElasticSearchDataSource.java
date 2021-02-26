package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchDataSource extends DataSource {
    private final String host;
    private final int port;
    private final String username;
    private final String password;


    @JsonCreator
    public ElasticSearchDataSource(@JsonProperty("id") long id,
                                   @JsonProperty("host") String host,
                                   @JsonProperty("port") int port,
                                   @JsonProperty("username") String username,
                                   @JsonProperty("password") String password) {
        super(id, Type.ELASTICSEARCH);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "ElasticSearchDataSource{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String host;
        private int port;
        private String username;
        private String password;
        private long id;

        private Builder() {
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
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

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public ElasticSearchDataSource build() {
            return new ElasticSearchDataSource(id, host, port, username, password);
        }
    }

}
