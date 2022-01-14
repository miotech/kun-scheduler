package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetColumnUpdateRequest {

    private String description;

    public DatasetColumnUpdateRequest() {
    }

    @JsonCreator
    public DatasetColumnUpdateRequest(@JsonProperty("description") String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
