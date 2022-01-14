package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DatasetUpdateRequest {

    private final String description;

    private final List<String> owners;

    private final List<String> tags;

    @JsonCreator
    public DatasetUpdateRequest(@JsonProperty("description") String description,
                                @JsonProperty("owners") List<String> owners,
                                @JsonProperty("tags") List<String> tags) {
        this.description = description;
        this.owners = owners;
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getOwners() {
        return owners;
    }

    public List<String> getTags() {
        return tags;
    }
}
