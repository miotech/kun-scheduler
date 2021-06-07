package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DatasetColumnHintResponse {

    private String databaseName;

    private String tableName;

    private List<String> columns;

    @JsonCreator
    public DatasetColumnHintResponse(@JsonProperty("databaseName") String databaseName,
                                     @JsonProperty("tableName") String tableName,
                                     @JsonProperty("columns") List<String> columns) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
