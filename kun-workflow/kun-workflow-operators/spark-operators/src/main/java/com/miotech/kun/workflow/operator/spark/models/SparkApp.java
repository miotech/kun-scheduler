package com.miotech.kun.workflow.operator.spark.models;

import java.util.List;

public class SparkApp {
    int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public AppInfo getAppInfo4() {
        return appInfo4;
    }

    public void setAppInfo4(AppInfo appInfo4) {
        this.appInfo4 = appInfo4;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    String state;
    String appId;
    AppInfo appInfo4;
    List<String> log;
    String kind;
}

