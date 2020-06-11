package com.miotech.kun.metadata.model;

public class PostgresDataSource extends DataSource {

    private final String url;

    private final String username;

    private final String password;

    public PostgresDataSource(long id, String url, String username, String password) {
        super(id, Type.Postgres);
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

        public static Builder aPostgresDataSource() {
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

        public Builder withId(long id) {
            this.id = id;
            return this;
        }


        public PostgresDataSource build() {
            return new PostgresDataSource(id, url, username, password);
        }
    }
}
