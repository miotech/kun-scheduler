package com.miotech.kun.workflow.core.model.entity;

public class PostgresCluster extends Cluster {

    private String url;

    private String username;

    private String password;

    public PostgresCluster(String url, String username, String password) {
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
        return null;
    }

    public static PostgresCluster.Builder newBuilder() {
        return new PostgresCluster.Builder();
    }

    public static final class Builder {
        private String url;
        private String username;
        private String password;

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

        public PostgresCluster build() {
            return new PostgresCluster(url, username, password);
        }
    }
}
