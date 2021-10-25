package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HiveTableStore extends DataStore {

    private final String location;

    private final String database;

    private final String table;

    public String getLocation() {
        return location;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    @JsonCreator
    public HiveTableStore(@JsonProperty("location") String location,
                          @JsonProperty("database") String database,
                          @JsonProperty("table") String table) {
        super(DataStoreType.HIVE_TABLE);
        this.location = location;
        this.database = database;
        //hive not support upperCase table name
        this.table = toLowCase(table);
    }

    @Override
    public String getDatabaseName() {
        return getDatabase();
    }

    @Override
    public String getLocationInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(database).append(":").append(table);
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HiveTableStore)) return false;
        HiveTableStore that = (HiveTableStore) o;
        return Objects.equals(getLocation(), that.getLocation()) &&
                Objects.equals(getDatabase(), that.getDatabase()) &&
                Objects.equals(getTable(), that.getTable());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocation(), getDatabase(), getTable());
    }

    @Override
    public ConnectionInfo getConnectionInfo() {
        Map<String,Object> values = new HashMap<>();
        return new ConnectionInfo(values);
    }

    @Override
    public String getName() {
        return table;
    }

    private String toLowCase(String table){
        return table.toLowerCase();
    }
}
