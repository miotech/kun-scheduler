package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.core.model.connection.ConnectionType;

public class DatabaseIdentifierProcessor {

    private DatabaseIdentifierProcessor() {
    }

    public static String escape(String name, ConnectionType dbType) {
        switch (dbType) {
            case ATHENA:
            case POSTGRESQL:
                return "\"" + name + "\"";
            case HIVE_SERVER:
            case PRESTO:
                return "`" + name + "`";
            default:
                return name;
        }
    }

}
