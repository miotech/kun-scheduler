package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.annotation.Nullable;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class DataStore implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335452366L;

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
}
