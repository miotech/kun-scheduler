package com.miotech.kun.metadata.client;

import com.google.common.collect.Maps;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;

/**
 * Universal JDBC connection tool, which adapts to Hive and Postgres
 * @author zhangxinwei
 */
public class JDBCClient {
    private static Logger logger = LoggerFactory.getLogger(JDBCClient.class);
    private static final Map<String, DataSource> dataSourceCache = Maps.newConcurrentMap();

    public static Connection getConnection(DatabaseType dbType, String url, String user, String password)
            throws ClassNotFoundException, SQLException {
        if (dbType == null) {
            throw new RuntimeException("dbType must not be null");
        }

        String driverName = selectSpecificDriver(dbType);
        Class.forName(driverName);
        return DriverManager.getConnection(url, user, password);
    }

    public static DataSource getDataSource(String url, String username, String password, DatabaseType dbType) {
        String key = url + username + password;
        if (dataSourceCache.containsKey(key)) {
            return dataSourceCache.get(key);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(selectSpecificDriver(dbType));
        HikariDataSource hikariDataSource = new HikariDataSource(config);

        dataSourceCache.put(key, hikariDataSource);
        return hikariDataSource;
    }

    private static String selectSpecificDriver(DatabaseType dbType) {
        switch (dbType) {
            case HIVE:
                return "org.apache.hive.jdbc.HiveDriver";
            case MYSQL:
                return "com.mysql.jdbc.Driver";
            case POSTGRES:
                return "org.postgresql.Driver";
            case PRESTO:
                return "io.prestosql.jdbc.PrestoDriver";
            case ATHENA:
                return "com.simba.athena.jdbc.Driver";
            default:
                throw new RuntimeException("invalid dbType: " + dbType.getName());
        }
    }

    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqlException) {
            logger.error("JDBCClient release db resources exception", sqlException);
        }
    }

}
