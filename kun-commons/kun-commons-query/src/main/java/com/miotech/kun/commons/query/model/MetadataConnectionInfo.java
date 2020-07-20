package com.miotech.kun.commons.query.model;

import org.json.simple.JSONObject;

/**
 * @author: Jie Chen
 * @created: 2020/7/12
 */
public class MetadataConnectionInfo {

    private JSONObject connectionInfo;

    private String urlPostfix;

    private Long datasourceId;

    private String datasourceType;

    private String database;

    public JSONObject getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(JSONObject connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getUrlPostfix() {
        return urlPostfix;
    }

    public void setUrlPostfix(String urlPostfix) {
        this.urlPostfix = urlPostfix;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Long datasourceId) {
        this.datasourceId = datasourceId;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
