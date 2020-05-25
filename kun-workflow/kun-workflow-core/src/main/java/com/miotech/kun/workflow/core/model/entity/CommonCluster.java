package com.miotech.kun.workflow.core.model.entity;

public class CommonCluster extends Cluster{

    final String hostname;
    final int port;
    final String username;
    final String password;

    public CommonCluster(String hostname, int port, String username, String password){
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHostname() {
        return hostname;
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
        return null;
    }

    public static CommonCluster.Builder newBuilder() {
        return new CommonCluster.Builder();
    }

    public static final class Builder {
        String hostname;
        int port;
        String username;
        String password;

        private Builder() {
        }

        public Builder withHostname(String hostname) {
            this.hostname = hostname;
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

        public CommonCluster build() {
            CommonCluster commonCluster = new CommonCluster(this.hostname, this.port, this.username, this.password);
            return commonCluster;
        }
    }
}
