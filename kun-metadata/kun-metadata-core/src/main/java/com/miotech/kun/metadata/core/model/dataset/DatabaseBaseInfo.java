package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseBaseInfo {

    private final String name;

    @JsonCreator
    public DatabaseBaseInfo(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
