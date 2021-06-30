package com.miotech.kun.metadata.core.model;

public class DatasetColumnSuggestRequest {

    private String databaseName;

    private String tableName;

    private String prefix;

    public DatasetColumnSuggestRequest(String databaseName, String tableName, String prefix) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.prefix = prefix;
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


}
