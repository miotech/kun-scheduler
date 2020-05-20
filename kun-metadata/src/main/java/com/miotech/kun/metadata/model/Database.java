package com.miotech.kun.metadata.model;

import com.miotech.kun.metadata.constant.DatabaseType;

public class Database {

    private Long id;

    private String name;

    private DatabaseType databaseType;

    private String url;

    private String username;

    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
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
        private Long id;
        private String name;
        private DatabaseType databaseType;
        private String url;
        private String username;
        private String password;

        private Builder() {
        }

        public static Builder getInstance() {
            return new Builder();
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDatabaseType(DatabaseType databaseType) {
            this.databaseType = databaseType;
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

        public Database build() {
            Database database = new Database();
            database.setId(id);
            database.setName(name);
            database.setDatabaseType(databaseType);
            database.setUrl(url);
            database.setUsername(username);
            database.setPassword(password);
            return database;
        }
    }
}
