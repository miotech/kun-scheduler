package com.miotech.kun.metadata.extract.tool;

import com.miotech.kun.metadata.constant.DatabaseType;

public class DatabaseIdentifierProcessor {

    public static String processTableNameIdentifier(String table, DatabaseType dbType) {
        switch (dbType) {
            case ATHENA:
                if (table.startsWith("_")) {
                    return "`" + table + "`";
                } else if (Character.isDigit(table.charAt(0))) {
                    return "\"" + table + "\"";
                } else {
                    return table;
                }
            case HIVE:
            case POSTGRES:
            case PRESTO:
                return "`" + table + "`";
            default:
                return table;
        }
    }

    public static String processFieldNameIdentifier(String fieldName, DatabaseType databaseType) {
        if (databaseType.equals(DatabaseType.ATHENA)) {
            return  "\"" + fieldName + "\"";
        } else if (databaseType.equals(DatabaseType.HIVE)) {
            return  "`" + fieldName + "`";
        } else {
            return fieldName;
        }
    }

}
