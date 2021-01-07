package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.databuilder.constant.DatabaseType;

public class DatabaseIdentifierProcessor {

    private DatabaseIdentifierProcessor() {
    }

    public static String escape(String name, DatabaseType dbType) {
        switch (dbType) {
            case ATHENA:
            case POSTGRES:
                return "\"" + name + "\"";
            case HIVE:
            case PRESTO:
                return "`" + name + "`";
            default:
                return name;
        }
    }

}
