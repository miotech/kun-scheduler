package com.miotech.kun.commons.query.model;

import org.json.simple.JSONObject;

/**
 * @author: Jie Chen
 * @created: 2020/7/12
 */
public class MetadataConnectionInfo {

    private JSONObject connectionInfo;

    private String urlPostfix;

    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
