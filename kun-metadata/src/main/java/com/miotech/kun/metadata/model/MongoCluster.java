package com.miotech.kun.metadata.model;

public class MongoCluster extends Cluster {

    private final String url;

    private final String username;

    private final String password;

    public MongoCluster(long clusterId, String url, String username, String password) {
        super(clusterId);
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

    public static MongoCluster.Builder newBuilder() {
        return new MongoCluster.Builder();
    }

    @Override
    public String toString() {
        return "MongoCluster{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static final class Builder {
        private String url;
        private String username;
        private String password;
        private long clusterId;

        private Builder() {
        }

        public static Builder aMongoCluster() {
            return new Builder();
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

        public Builder withClusterId(long clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public MongoCluster build() {
            return new MongoCluster(clusterId, url, username, password);
        }
    }
}
