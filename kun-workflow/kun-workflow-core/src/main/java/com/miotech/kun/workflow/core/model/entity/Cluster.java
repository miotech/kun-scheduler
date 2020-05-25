package com.miotech.kun.workflow.core.model.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@JsonTypeInfo(use = CLASS, include = PROPERTY, property = "@class")
public abstract class Cluster {

    private final long clusterId;

    public long getClusterId() {
        return clusterId;
    }

    public Cluster(long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public abstract String toString();
}
