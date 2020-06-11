package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AthenaDataStore extends DataStore {

    private final String url;

    private final String database;

    private final String table;

    @JsonCreator
    public AthenaDataStore(@JsonProperty("url") String url,
                           @JsonProperty("database") String database,
                           @JsonProperty("table") String table) {
        super(DataStoreType.TABLE);
        this.url = url;
        this.database = database;
        this.table = table;
    }

    public String getUrl() {
        return url;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }
}
