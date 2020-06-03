package com.miotech.kun.metadata.client;

import com.beust.jcommander.internal.Lists;
import com.miotech.kun.metadata.constant.DatabaseType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class JDBCClientTest {
    private static Logger logger = LoggerFactory.getLogger(JDBCClientTest.class);

    @Test
    public void testGetConnection_presto() {
        Connection connection = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.PRESTO, "jdbc:presto://10.0.0.85:8073/hive", "root", null);
            Assert.assertNotNull(connection);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            JDBCClient.close(connection, null, null);
        }
    }

    @Test
    public void testGetConnection_hive() {
        Connection connection = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.HIVE, "jdbc:hive2://10.0.0.85:10000/sys", "hive", null);
            Assert.assertNotNull(connection);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            JDBCClient.close(connection, null, null);
        }
    }

    @Test
    public void testShowDatabases_presto() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.PRESTO, "jdbc:presto://10.0.0.85:8073/hive", "root", null);

            String scanCluster = "select companyname from dw.sp_ciqcompanytest";
            statement = connection.createStatement();

            resultSet = statement.executeQuery(scanCluster);
            List<String> databases = Lists.newArrayList();
            while (resultSet.next()) {
                String database = resultSet.getString(1);
                logger.info("database:" + database);
                databases.add(database);
            }
            assertThat(databases, containsInAnyOrder("default", "dm", "dw", "sys"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    @Test
    public void testShowDatabases_hive() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.HIVE, "jdbc:hive2://13.231.163.20:10000", "hive", null);
            String scanCluster = "SHOW DATABASES";
            statement = connection.createStatement();

            resultSet = statement.executeQuery(scanCluster);

            List<String> databases = Lists.newArrayList();
            while (resultSet.next()) {
                String database = resultSet.getString(1);
                logger.info("database:" + database);
                databases.add(database);
            }
//            assertThat(databases, containsInAnyOrder("default", "dm", "dw", "sys"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    @Test
    public void testShowTables_presto() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.PRESTO, "jdbc:presto://10.0.0.85:8073/hive/dm", "root", null);
            String scanDatabase = "show tables";
            statement = connection.createStatement();

            resultSet = statement.executeQuery(scanDatabase);
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                logger.info("table:" + table);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    @Test
    public void testShowTables_hive() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.HIVE, "jdbc:hive2://10.0.0.85:10000", "hive", null);
            String scanDatabase = "show tables from default";
            statement = connection.createStatement();

            resultSet = statement.executeQuery(scanDatabase);
            List<String> tables = Lists.newArrayList();
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                logger.info("table:" + table);
                tables.add(table);
            }
            assertThat(tables, is(7010));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    @Test
    public void testTableStat_athena() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.ATHENA, "jdbc:awsathena://athena.ap-northeast-1.amazonaws.com:443;S3OutputLocation=s3://com.miotech.data.prd/Database/DEFAULT/", "AKIAIL42HPN4LO3XUIHQ", "yFfJ74UD80NWmPuhH2dLKr2JYJU8RU/qj0QVzOE8");
            String sql = "select count(*) from (select tag from dm.\"2017_ratio\" group by tag) t1";
//            String sql = "select count(*) from (select companyid from dw.a_shareholder_foreign group by companyid) t1";
            statement = connection.createStatement();

            resultSet = statement.executeQuery(sql);
            long rowCount = 0;
            while (resultSet.next()) {
                rowCount = resultSet.getLong(1);
                logger.info("rowCount:" + rowCount);
            }

            assertThat(rowCount, is(7010L));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

}
