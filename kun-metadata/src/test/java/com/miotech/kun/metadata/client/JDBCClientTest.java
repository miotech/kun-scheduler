package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.constant.DatabaseType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCClientTest {

    @org.testng.annotations.Test
    public void testGetConnection() {
        try {
            Connection connection = JDBCClient.getConnection(DatabaseType.MYSQL, null, null, null);
            ResultSet columns = connection.getMetaData().getColumns(null, null, null, null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

}
