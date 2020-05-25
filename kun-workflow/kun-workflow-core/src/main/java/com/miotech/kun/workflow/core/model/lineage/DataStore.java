package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.workflow.core.model.entity.Cluster;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@JsonTypeInfo(use = CLASS, include = PROPERTY, property = "@class")
public abstract class DataStore {

    private final DataStoreType type;

    public DataStoreType getType() {
        return type;
    }

    public DataStore(DataStoreType type, Cluster cluster) {
        this.type = type;
    }
}
