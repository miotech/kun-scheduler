package com.miotech.kun.metadata.databuilder.client;

import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.util.NoSuchElementException;

public class HiveMetaStoreClient {

    private HiveMetaStoreClient() {
    }

    public static String parseOutputFormat(Type metaStoreType, DataSource dataSource, String databaseName, String tableName) {
        switch (metaStoreType) {
            case GLUE:
                Table table = GlueClient.searchTable((AWSDataSource) dataSource, databaseName, tableName);
                if (table == null) {
                    throw new NoSuchElementException("Table not found");
                }
                return table.getStorageDescriptor().getOutputFormat();

            case MYSQL:
                return "";
            default:
                throw new IllegalArgumentException("Invalid metaStoreType: " + metaStoreType.name());
        }
    }

    public static String parseLocation(Type metaStoreType, DataSource dataSource, String databaseName, String tableName) {
        switch (metaStoreType) {
            case GLUE:
                Table table = GlueClient.searchTable((AWSDataSource) dataSource, databaseName, tableName);
                if (table == null) {
                    throw new NoSuchElementException("Table not found");
                }
                return table.getStorageDescriptor().getLocation();

            case MYSQL:
                return "";
            default:
                throw new IllegalArgumentException("Invalid metaStoreType: " + metaStoreType.name());
        }
    }

    public enum Type {
        GLUE, MYSQL;
    }


}
