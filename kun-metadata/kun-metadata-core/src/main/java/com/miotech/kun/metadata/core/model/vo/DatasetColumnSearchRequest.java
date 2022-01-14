package com.miotech.kun.metadata.core.model.vo;

public class DatasetColumnSearchRequest extends PageInfo {

    private String keyword;

    public DatasetColumnSearchRequest() {
    }

    public DatasetColumnSearchRequest(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
