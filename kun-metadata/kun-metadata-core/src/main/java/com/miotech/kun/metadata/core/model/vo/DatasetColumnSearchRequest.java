package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetColumnSearchRequest extends PageInfo {

    private String keyword;

    public DatasetColumnSearchRequest() {
    }

    @JsonCreator
    public DatasetColumnSearchRequest(@JsonProperty("keyword") String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
