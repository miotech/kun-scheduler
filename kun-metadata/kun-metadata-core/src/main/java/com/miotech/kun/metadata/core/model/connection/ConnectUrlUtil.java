package com.miotech.kun.metadata.core.model.connection;

import com.google.common.base.Preconditions;

public class ConnectUrlUtil {

    private static final String JDBC_CONN_URL = "jdbc:%s://%s:%d/";
    private static final String MONGO_CONN_URL = "mongodb://%s:%s@%s:%d";
    private static final String MONGO_SIMPLE_CONN_URL = "mongodb://%s:%d";
    public static final String POSTGRESQL_PREFIX = "postgresql";
    public static final String HIVE_PREFIX = "hive2";
    public static final String MYSQL_PREFIX = "mysql";
    public static final String COLON = ":";

    private ConnectUrlUtil() {
    }

    public static String convertToConnectUrl(String host, int port, ConnectionType dbType) {
        Preconditions.checkNotNull(dbType, "DatabaseType should not be null");
        switch (dbType) {
            case POSTGRESQL:
                return String.format(JDBC_CONN_URL, POSTGRESQL_PREFIX, host, port);
            case HIVE_SERVER:
                return String.format(JDBC_CONN_URL, HIVE_PREFIX, host, port);
            case MYSQL:
                return String.format(JDBC_CONN_URL, MYSQL_PREFIX, host, port);
            case ELASTICSEARCH:
            case ARANGO:
                return host + COLON + port;
            case MONGODB:
                return String.format(MONGO_SIMPLE_CONN_URL, host, port);
            default:
                throw new IllegalArgumentException("Invalid DatabaseType: " + dbType);
        }
    }

}
