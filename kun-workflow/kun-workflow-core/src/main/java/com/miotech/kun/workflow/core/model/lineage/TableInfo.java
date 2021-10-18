package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = TableInfo.TableInfoBuilder.class)
public class TableInfo {

    private final String tableName;
    private final String databaseName;

    public TableInfo(String tableName, String databaseName) {
        this.tableName = tableName;
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static TableInfoBuilder newBuilder(){
        return new TableInfoBuilder();
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "tableName='" + tableName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                '}';
    }

    @JsonPOJOBuilder
    public static final class TableInfoBuilder {
        private String tableName;
        private String databaseName;

        private TableInfoBuilder() {
        }

        public TableInfoBuilder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public TableInfoBuilder withDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public TableInfo build() {
            return new TableInfo(tableName, databaseName);
        }
    }
}
