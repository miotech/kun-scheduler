package com.miotech.kun.commons.query.model;

/**
 * @author: Jie Chen
 * @created: 2020/7/10
 */
public class JDBCConnectionInfo {

    private String url;

    private String username;

    private String password;

    private String driverClass;

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

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }
}
