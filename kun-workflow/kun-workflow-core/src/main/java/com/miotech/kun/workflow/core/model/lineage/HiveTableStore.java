package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HiveTableStore extends DataStore {

    private final String dataStoreUrl;

    private final String database;

    private final String table;

    public String getDataStoreUrl() {
        return dataStoreUrl;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    @JsonCreator
    public HiveTableStore(@JsonProperty("dataStoreUrl") String dataStoreUrl,
                          @JsonProperty("database") String database,
                          @JsonProperty("table") String table) {
        super(DataStoreType.HIVE_TABLE);
        this.dataStoreUrl = dataStoreUrl;
        this.database = database;
        this.table = table;
    }
}
