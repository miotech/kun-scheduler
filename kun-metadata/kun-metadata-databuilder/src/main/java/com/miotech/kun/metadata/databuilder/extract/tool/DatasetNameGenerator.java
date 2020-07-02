package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import io.prestosql.jdbc.$internal.guava.base.Preconditions;

public class DatasetNameGenerator {

    public static String generateDatasetName(DatabaseType databaseType, String table) {
        Preconditions.checkNotNull(databaseType, "invalid DatabaseType: null");

        switch (databaseType) {
            case HIVE:
            case POSTGRES:
                return "db." + table;
            default:
                return table;
        }
    }

}
