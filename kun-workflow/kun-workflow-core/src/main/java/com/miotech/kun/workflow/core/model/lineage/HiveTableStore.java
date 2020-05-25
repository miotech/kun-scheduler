package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.workflow.core.model.entity.HiveCluster;

public class HiveTableStore extends DataStore {

    private final String database;

    private final String table;

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    @JsonCreator
    public HiveTableStore(@JsonProperty("database") String database,
                          @JsonProperty("table") String table,
                          @JsonProperty("cluster") HiveCluster cluster) {
        super(DataStoreType.TABLE, cluster);
        this.database = database;
        this.table = table;
    }
}
