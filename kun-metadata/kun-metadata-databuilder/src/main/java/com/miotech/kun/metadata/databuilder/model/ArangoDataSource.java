package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArangoDataSource extends DataSource{
    final String url;
    final String username;
    final String password;

    @JsonCreator
    public ArangoDataSource(@JsonProperty("id") long id,
                            @JsonProperty("url") String url,
                            @JsonProperty("username") String username,
                            @JsonProperty("password") String password) {
        super(id, Type.ARANGO);
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
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
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static ArangoDataSourceBuilder newBuilder() {
        return new ArangoDataSourceBuilder();
    }


    public static final class ArangoDataSourceBuilder {
        String url;
        String username;
        String password;
        private long id;

        private ArangoDataSourceBuilder() {
        }

        public ArangoDataSourceBuilder withUrl(String url) {
            this.url = url;
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
            return new ArangoDataSource(id, url, username, password);
        }
    }
}
