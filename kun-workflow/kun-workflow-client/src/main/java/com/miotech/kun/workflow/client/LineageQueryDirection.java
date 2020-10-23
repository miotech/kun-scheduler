package com.miotech.kun.workflow.client;

public enum LineageQueryDirection {
    UPSTREAM("UPSTREAM"),
    DOWNSTREAM("DOWNSTREAM"),
    BOTH("BOTH");

    private final String queryParam;

    LineageQueryDirection(String queryParam) {
        this.queryParam = queryParam;
    }

    public String getQueryParam() {
        return this.queryParam;
    }
}
