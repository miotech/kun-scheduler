package com.miotech.kun.metadata.model;

public class DatasetStatRequest {

    private String table;

    private String url;

    private String username;

    private String password;

    public DatasetStatRequest(String table, String url, String username, String password) {
        this.table = table;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getTable() {
        return table;
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

    public static DatasetStatRequest.Builder newBuilder() {
        return new DatasetStatRequest.Builder();
    }

    public static final class Builder {
        private String table;
        private String url;
        private String username;
        private String password;

        private Builder() {
        }

        public Builder withTable(String table) {
            this.table = table;
            return this;
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

        public DatasetStatRequest build() {
            return new DatasetStatRequest(table, url, username, password);
        }
    }
}
