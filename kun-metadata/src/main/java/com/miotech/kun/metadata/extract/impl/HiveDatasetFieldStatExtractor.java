package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.extract.DatasetFieldStatExtractor;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetFieldStatRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HiveDatasetFieldStatExtractor implements DatasetFieldStatExtractor {

    private DatasetFieldStatRequest fieldStatRequest;
    private Connection connection;

    public HiveDatasetFieldStatExtractor(DatasetFieldStatRequest fieldStatRequest, Connection connection) {
        this.fieldStatRequest = fieldStatRequest;
        this.connection = connection;
    }

    @Override
    public DatasetFieldStat extract() {
        DatasetFieldStat result = new DatasetFieldStat();
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            String sql = "SELECT COUNT(*) FROM (SELECT ? FROM ? GROUP BY ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, fieldStatRequest.getField());
            statement.setString(2, fieldStatRequest.getTable());
            statement.setString(3, fieldStatRequest.getField());
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long distinctCount = resultSet.getLong(0);
                result.setDistinctCount(distinctCount);
            }

            sql = "SELECT COUNT(*) FROM ? WHERE ? IS NOT NULL)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, fieldStatRequest.getTable());
            statement.setString(2, fieldStatRequest.getField());
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long nonnullCount = resultSet.getLong(0);
                result.setNonnullCount(nonnullCount);
            }

        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(statement, resultSet);
        }

        return result;
    }
}
