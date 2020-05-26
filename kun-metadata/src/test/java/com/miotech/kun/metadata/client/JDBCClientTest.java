package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.constant.DatabaseType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCClientTest {
    private static Logger logger = LoggerFactory.getLogger(JDBCClientTest.class);

    @Test
    public void testGetConnection() {
        try {
            Connection connection = JDBCClient.getConnection(DatabaseType.PRESTO, "jdbc:presto://10.0.0.85:8073/hive", "root", null);
            Assert.assertNotNull(connection);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Test
    public void testShowDatabases() {
        try {
            Connection connection = JDBCClient.getConnection(DatabaseType.PRESTO, "jdbc:presto://10.0.0.85:8073/hive", "root", null);

            String scanCluster = "show databases";
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(scanCluster);
            while (resultSet.next()) {
                String database = resultSet.getString(1);
                logger.info("database:" + database);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Test
    public void testShowTables() {
        try {
            Connection connection = JDBCClient.getConnection(DatabaseType.PRESTO, "jdbc:presto://10.0.0.85:8073/hive/default", "root", null);
            String scanDatabase = "show tables";
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(scanDatabase);
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                logger.info("table:" + table);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Test
    public void testTableStat() {
        try {
            Connection connection = JDBCClient.getConnection(DatabaseType.HIVE, "jdbc:hive2://10.0.0.85:10000/sys", "hive", null);
            String sql = "SELECT COUNT(*) FROM dbs";
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                long rowCount = resultSet.getLong(1);
                logger.info("rowCount:" + rowCount);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

}
