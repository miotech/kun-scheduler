package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.workflow.core.model.common.URI;

import javax.annotation.Nullable;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@JsonTypeInfo(use = CLASS, include = PROPERTY, property = "@class")
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
