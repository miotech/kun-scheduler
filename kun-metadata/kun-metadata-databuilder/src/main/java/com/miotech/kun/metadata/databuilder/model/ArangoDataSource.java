package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArangoDataSource extends DataSource{
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    @JsonCreator
    public ArangoDataSource(@JsonProperty("id") long id,
                            @JsonProperty("host") String host,
                            @JsonProperty("port") int port,
                            @JsonProperty("username") String username,
                            @JsonProperty("password") String password) {
        super(id, Type.ARANGO);
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
        return "ArangoDataSource{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static ArangoDataSourceBuilder newBuilder() {
        return new ArangoDataSourceBuilder();
    }

    public static final class ArangoDataSourceBuilder {
        private String host;
        private int port;
        private String username;
        private String password;
        private long id;

        private ArangoDataSourceBuilder() {
        }

        public ArangoDataSourceBuilder withHost(String host) {
            this.host = host;
            return this;
        }

        public ArangoDataSourceBuilder withPort(int port) {
            this.port = port;
            return this;
        }

        public ArangoDataSourceBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public ArangoDataSourceBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public ArangoDataSourceBuilder withId(long id) {
            this.id = id;
            return this;
        }

        public ArangoDataSource build() {
            return new ArangoDataSource(id, host, port, username, password);
        }
    }
}
