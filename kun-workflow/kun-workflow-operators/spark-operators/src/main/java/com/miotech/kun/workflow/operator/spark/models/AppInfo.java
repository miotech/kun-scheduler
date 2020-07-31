package com.miotech.kun.workflow.operator.spark.models;

public class AppInfo {
    public String getDriverLogUrl() {
        return driverLogUrl;
    }

    public void setDriverLogUrl(String driverLogUrl) {
        this.driverLogUrl = driverLogUrl;
    }

    public String getSparkUiUrl() {
        return sparkUiUrl;
    }

    public void setSparkUiUrl(String sparkUiUrl) {
        this.sparkUiUrl = sparkUiUrl;
    }

    String driverLogUrl;
        String sparkUiUrl;
}
