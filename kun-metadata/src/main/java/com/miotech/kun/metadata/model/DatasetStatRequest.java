package com.miotech.kun.metadata.model;

public class DatasetStatRequest {

    private String table;

    private String url;

    private String username;

    private String password;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public static final class Builder {
        private String table;
        private String url;
        private String username;
        private String password;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder setTable(String table) {
            this.table = table;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public DatasetStatRequest build() {
            DatasetStatRequest datasetStatRequest = new DatasetStatRequest();
            datasetStatRequest.setTable(table);
            datasetStatRequest.setUrl(url);
            datasetStatRequest.setUsername(username);
            datasetStatRequest.setPassword(password);
            return datasetStatRequest;
        }
    }
}
