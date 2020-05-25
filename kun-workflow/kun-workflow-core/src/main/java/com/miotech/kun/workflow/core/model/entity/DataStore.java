package com.miotech.kun.workflow.core.model.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@JsonTypeInfo(use = CLASS, include = PROPERTY, property = "@class")
public class DataStore {

    private final DataStoreType type;
    private Cluster cluster;

    public DataStoreType getType() {
        return type;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public DataStore(DataStoreType type, Cluster cluster) {
        this.type = type;
        this.cluster = cluster;
    }

}
