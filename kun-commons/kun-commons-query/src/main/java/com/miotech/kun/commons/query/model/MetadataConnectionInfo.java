package com.miotech.kun.commons.query.model;

import org.json.simple.JSONObject;

/**
 * @author: Jie Chen
 * @created: 2020/7/12
 */
public class MetadataConnectionInfo {

    private JSONObject connectionInfo;

    private String databaseName;

    private String type;

    public JSONObject getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(JSONObject connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
