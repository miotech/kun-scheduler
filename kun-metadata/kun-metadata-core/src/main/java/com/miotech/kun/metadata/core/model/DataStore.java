package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.metadata.core.common.URI;

import javax.annotation.Nullable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class DataStore {

    private final DataStoreType type;

    public DataStoreType getType() {
        return type;
    }

    /**
     * extract database and schema info
     * @return
     */
    @JsonIgnore
    @Nullable
    public abstract String getDatabaseName();

    public DataStore(DataStoreType type) {
        this.type = type;
    }

    public abstract URI getURI();
}
