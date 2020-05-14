package com.miotech.kun.workflow.core.model.entity;

import com.miotech.kun.workflow.utils.HashUtils;

public class TableEntity extends Entity {
    private final Cluster cluster;
    private final String database;
    private final String tableName;

    public TableEntity(Cluster cluster, String database, String tableName) {
        super(EntityType.TABLE);
        this.cluster = cluster;
        this.database = database;
        this.tableName = tableName;
    }

    @Override
    protected String identifierPart() {
        return HashUtils.murmur(cluster.toString()) + "." + database + "." + tableName;
    }
}
