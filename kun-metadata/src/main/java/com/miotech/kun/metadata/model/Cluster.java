package com.miotech.kun.metadata.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@JsonTypeInfo(use = CLASS, include = PROPERTY, property = "@class")
public abstract class Cluster {

    private final long clusterId;

    public final long getClusterId() {
        return clusterId;
    }

    public Cluster(long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public abstract String toString();
}
