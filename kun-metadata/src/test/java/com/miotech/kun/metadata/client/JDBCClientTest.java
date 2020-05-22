package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.constant.DatabaseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCClientTest {

    @org.testng.annotations.Test
    public void testGetConnection() {
        try {
            Connection connection = JDBCClient.getConnection(DatabaseType.HIVE, "jdbc:hive2://10.0.0.85:10000", null, null);
//            Connection connection = JDBCClient.getConnection(DatabaseType.HIVE, "jdbc:hive2://13.231.163.20:10000", "1", "1");
            /*String choiceDatabase = "use normalize";
            PreparedStatement choiceDatabaseStatement = connection.prepareStatement(choiceDatabase);
            choiceDatabaseStatement.execute();*/

            String scanDatabase = "show databases";
            PreparedStatement statement = connection.prepareStatement(scanDatabase);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String database = resultSet.getString(1);
                System.out.println("================" + database);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

}
