package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.DatasetStatExtractor;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.metadata.model.DatasetStatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


public class HiveDatasetStatExtractor implements DatasetStatExtractor {
    private static Logger logger = LoggerFactory.getLogger(HiveDatasetStatExtractor.class);
    private DatasetStatRequest datasetStatRequest;

    public HiveDatasetStatExtractor(DatasetStatRequest datasetStatRequest) {
        this.datasetStatRequest = datasetStatRequest;
    }

    @Override
    public DatasetStat extract() {
        DatasetStat datasetStat = new DatasetStat();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.HIVE, datasetStatRequest.getUrl(),
                    datasetStatRequest.getUsername(), datasetStatRequest.getPassword());
            String sql = "SELECT count(*) FROM ?";
            statement = connection.prepareStatement(sql);

            statement.setString(1, datasetStatRequest.getTable());
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long rowCount = resultSet.getLong(0);
                datasetStat.setRowCount(rowCount);
                datasetStat.setStatDate(new Date());
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.HIVE.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return datasetStat;
    }
}
