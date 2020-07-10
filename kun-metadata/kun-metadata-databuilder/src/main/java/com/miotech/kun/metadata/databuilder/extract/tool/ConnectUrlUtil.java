package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import io.prestosql.jdbc.$internal.guava.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class ConnectUrlUtil {

    private static final String JDBC_CONN_URL = "jdbc:%s://%s:%d/";
    private static final String MONGO_CONN_URL = "mongodb://%s:%s@%s:%d";
    private static final String MONGO_SIMPLE_CONN_URL = "mongodb://%s:%d";
    public static final String POSTGRESQL_PREFIX = "postgresql";
    public static final String COLON = ":";

    private ConnectUrlUtil() {
    }

    public static String convertToConnectUrl(String host, int port, String username, String password, DatabaseType dbType) {
        Preconditions.checkNotNull(dbType, "DatabaseType should not be null");
        switch (dbType) {
            case POSTGRES:
                return String.format(JDBC_CONN_URL, POSTGRESQL_PREFIX, host, port);
            case ELASTICSEARCH:
            case ARANGO:
                return host + COLON + port;
            case MONGO:
                if (StringUtils.isBlank(username)) {
                    return String.format(MONGO_SIMPLE_CONN_URL, host, port);
                }
                return String.format(MONGO_CONN_URL, username, password, host, port);
            default:
                throw new IllegalArgumentException("Invalid DatabaseType: " + dbType);
        }
    }

}
