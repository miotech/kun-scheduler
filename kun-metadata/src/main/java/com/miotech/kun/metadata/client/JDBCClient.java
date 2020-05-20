package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.constant.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Universal JDBC connection tool, which adapts to Hive and Postgres
 * @author zhangxinwei
 */
public class JDBCClient {
    private static Logger logger = LoggerFactory.getLogger(JDBCClient.class);

    public static Connection getConnection(DatabaseType dbType, String url, String user, String password)
            throws ClassNotFoundException, SQLException {
        if (dbType == null) {
            throw new RuntimeException("dbType must not be null");
        }

        String driverName = selectSpecificDriver(dbType);
        Class.forName(driverName);
        return DriverManager.getConnection(url, user, password);
    }

    private static String selectSpecificDriver(DatabaseType dbType) {
        switch (dbType) {
            case HIVE:
                return "org.apache.hive.jdbc.HiveDriver";
            case MYSQL:
                return "com.mysql.jdbc.Driver";
            case POSTGRES:
                return "org.postgresql.Driver";
            default:
                throw new RuntimeException("invalid dbType: " + dbType.getName());
        }
    }

    public static void close(Statement statement, ResultSet resultSet) {
        try {
            if (statement != null) {
                statement.close();
            }

            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException sqlException) {
            logger.error("JDBCClient release db resources exception", sqlException);
        }
    }

}
