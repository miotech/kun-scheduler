package com.miotech.kun.metadata.common.connector;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.JdbcConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class JdbcConnector implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(JdbcConnector.class);

    private final JdbcConnectionInfo jdbcConnectionInfo;

    private Connection connection;

    public JdbcConnector(JdbcConnectionInfo jdbcConnectionInfo) {
        this.jdbcConnectionInfo = jdbcConnectionInfo;
    }

    protected Connection getJdbcConnection(String database,String schema) {
        String url = useSchema(jdbcConnectionInfo.getJdbcUrl(), database, schema);
        String user = jdbcConnectionInfo.getUsername();
        String password = jdbcConnectionInfo.getPassword();
        ConnectionType type = jdbcConnectionInfo.getConnectionType();
        if (type == null) {
            throw new RuntimeException("dbType must not be null");
        }

        String driverName = selectSpecificDriver(type);
        try {
            Class.forName(driverName);
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public synchronized ResultSet query(Query query) {
        connection = getJdbcConnection(query.getDatabase(),query.getSchema());
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(query.getSql());
            resultSet = statement.executeQuery();
            return resultSet;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public synchronized void close() {
        try {
            if(connection != null){
                connection.close();
            }
        } catch (Exception exception) {
            logger.error("connector release db resources exception", exception);
        }
    }

    public void close(AutoCloseable... closeables) {
        if (closeables == null) {
            return;
        }

        try {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Exception exception) {
            logger.error("connector release db resources exception", exception);
        }
    }

    private static String selectSpecificDriver(ConnectionType type) {
        switch (type) {
            case HIVE_THRIFT:
                return "org.apache.hive.jdbc.HiveDriver";
            case MYSQL:
                return "com.mysql.jdbc.Driver";
            case POSTGRESQL:
                return "org.postgresql.Driver";
            case PRESTO:
                return "io.prestosql.jdbc.PrestoDriver";
            case ATHENA:
                return "com.simba.athena.jdbc.Driver";
            default:
                throw new UnsupportedOperationException(String.format("invalid dbType: {}", type.name()));
        }
    }

    private String useDatabase(String url, String database) {
        url = fixUrl(url);
        return url.substring(0, url.lastIndexOf('/') + 1) + database;
    }

    private String useSchema(String url, String database, String schema) {
        if(database == null){
            return url;
        }
        url = fixUrl(url);
        url = useDatabase(url, database);
        if (schema == null) {
            return url;
        }
        return url + "?currentSchema=" + schema;
    }

    private String fixUrl(String url) {
        if (!url.contains("/")) {
            return url.concat("/");
        }

        return url;
    }
}
