package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.constant.DatabaseType;

import java.sql.SQLException;

public class JDBCClientTest {

    @org.testng.annotations.Test
    public void testGetConnection() {
        try {
            JDBCClient.getConnection(DatabaseType.HIVE, null, null, null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

}
