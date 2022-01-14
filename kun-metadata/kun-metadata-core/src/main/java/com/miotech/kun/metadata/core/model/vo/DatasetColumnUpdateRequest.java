package com.miotech.kun.metadata.core.model.vo;

public class DatasetColumnUpdateRequest {

    private String description;

    public DatasetColumnUpdateRequest() {
    }

    public DatasetColumnUpdateRequest(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
