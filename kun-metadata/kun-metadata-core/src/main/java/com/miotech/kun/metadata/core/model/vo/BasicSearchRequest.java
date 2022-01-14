package com.miotech.kun.metadata.core.model.vo;

public class BasicSearchRequest extends PageInfo {

    private String keyword;

    public BasicSearchRequest() {
    }

    public BasicSearchRequest(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

}
