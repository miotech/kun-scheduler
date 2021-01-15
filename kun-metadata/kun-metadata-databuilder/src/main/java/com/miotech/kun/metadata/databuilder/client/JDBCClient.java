package com.miotech.kun.metadata.databuilder.client;

import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * Universal JDBC connection tool, which adapts to Hive and Postgres
 * @author zhangxinwei
 */
public class JDBCClient {
    private static Logger logger = LoggerFactory.getLogger(JDBCClient.class);
    private JDBCClient() {
    }

    public static DataSource getDataSource(String url, String username, String password, DatabaseType dbType) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(selectSpecificDriver(dbType));
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setIdleTimeout(TimeUnit.SECONDS.toMillis(10));
        return new HikariDataSource(config);
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
                throw new UnsupportedOperationException(String.format("invalid dbType: {}", dbType.getName()));
        }
    }

    public static void close(AutoCloseable... closeables) {
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
            logger.error("JDBCClient release db resources exception", exception);
        }
    }

    public static Connection getConnection(String url, String user, String password, DatabaseType dbType) throws ClassNotFoundException, SQLException {
        if (dbType == null) {
            throw new RuntimeException("dbType must not be null");
        }

        String driverName = selectSpecificDriver(dbType);
        Class.forName(driverName);
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getConnection(com.miotech.kun.metadata.databuilder.model.DataSource dataSource, String dbName, String schemaName) throws SQLException, ClassNotFoundException {
        com.miotech.kun.metadata.databuilder.model.DataSource.Type dbType = dataSource.getType();
        switch (dbType) {
            case AWS:
                AWSDataSource awsDataSource = (AWSDataSource) dataSource;
                return getConnection(awsDataSource.getAthenaUrl(), awsDataSource.getAthenaUsername(), awsDataSource.getAthenaPassword(), DatabaseType.ATHENA);
            case HIVE:
                HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
                return getConnection(hiveDataSource.getDatastoreUrl(), hiveDataSource.getDatastoreUsername(), hiveDataSource.getDatastorePassword(), DatabaseType.HIVE);
            case POSTGRESQL:
                PostgresDataSource postgresDataSource = (PostgresDataSource) dataSource;
                return getConnection(UseDatabaseUtil.useSchema(postgresDataSource.getUrl(), dbName, schemaName), postgresDataSource.getUsername(), postgresDataSource.getPassword(), DatabaseType.POSTGRES);
            default:
                throw new UnsupportedOperationException(String.format("invalid dataSourceType: {}", dataSource.getType()));
        }
    }
}
