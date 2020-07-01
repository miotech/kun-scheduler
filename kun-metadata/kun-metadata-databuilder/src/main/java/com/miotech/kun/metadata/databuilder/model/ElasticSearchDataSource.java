package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticSearchDataSource extends DataSource{
    private final String url;
    private final String username;
    private final String password;


    @JsonCreator
    public ElasticSearchDataSource(@JsonProperty("id") long id,
                                @JsonProperty("url") String url,
                                @JsonProperty("username") String username,
                                @JsonProperty("password") String password) {
        super(id, Type.ELASTICSEARCH);
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
        return "ElasticSearchDataSource{" +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String url;
        private String username;
        private String password;
        private long id;

        private Builder() {
        }

        public Builder withUrl(String url) {
            this.url = url;
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
            return new ElasticSearchDataSource(id, url, username, password);
        }
    }

}
