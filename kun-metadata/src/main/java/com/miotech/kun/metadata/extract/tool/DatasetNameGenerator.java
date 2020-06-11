package com.miotech.kun.metadata.extract.tool;

import com.miotech.kun.metadata.constant.DatabaseType;

public class DatasetNameGenerator {

    public static String generateDatasetName(DatabaseType databaseType, String table) {
        if (databaseType == null) {
            throw new RuntimeException("invalid DatabaseType: null");
        }

        switch (databaseType) {
            case HIVE:
            case POSTGRES:
                return "db." + table;
            default:
                return table;
        }
    }

}
