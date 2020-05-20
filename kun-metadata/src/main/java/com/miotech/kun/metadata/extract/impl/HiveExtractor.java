package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.HiveDatabase;
import com.miotech.kun.metadata.model.bo.*;
import com.miotech.kun.metadata.models.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HiveExtractor extends JDBCExtractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private HiveDatabase hiveDatabase;

    private HiveExtractor() {};

    public HiveExtractor(HiveDatabase hiveDatabase) {
        init(hiveDatabase);
    }

    private void init(HiveDatabase hiveDatabase) {
        //TODO verify the integrity of the data
        this.hiveDatabase = hiveDatabase;
    }

    @Override
    public List<Dataset> extract() {
        List<Table> tables = new ArrayList<>();
        return null;
    }

    @Override
    public List<DatasetInfo> extractDataset(DatasetExtractBO extractBO) {
        List<DatasetInfo> result = new ArrayList<>();
        Connection connection;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(extractBO.getDatabaseType(), extractBO.getUrl(),
                    extractBO.getUsername(), extractBO.getPassword());
            statement = connection.createStatement();

            String sql = "show tables";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String tableName = resultSet.getString(0);
                DatasetInfo.Builder builder = new DatasetInfo.Builder();
                DatasetInfo datasetInfo = builder.setDatabaseId(extractBO.getDatabaseId()).setName(tableName).
                        setDataStore(new HiveDataStore(tableName)).build();
                result.add(datasetInfo);
            }

        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("ExtractDataset error: classNotFound", classNotFoundException);
        } catch (SQLException sqlException) {
            logger.error("ExtractDataset error: sqlException", sqlException);
        } finally {
            JDBCClient.close(statement, resultSet);
        }

        return result;
    }

    @Override
    public List<DatasetFieldInfo> extractFields(DatasetFieldExtractBO fieldExtractBO) {
        List<DatasetFieldInfo> result = new ArrayList<>();
        Connection connection;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(fieldExtractBO.getDatabaseType(), fieldExtractBO.getUrl(),
                    fieldExtractBO.getUsername(), fieldExtractBO.getPassword());
            statement = connection.createStatement();

            String sql = String.format("`desc` %s", fieldExtractBO.getTableName());
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String fieldName = resultSet.getString(0);
                String fieldType = resultSet.getString(1);

                DatasetFieldInfo.Builder builder = new DatasetFieldInfo.Builder();
                DatasetFieldInfo datasetFieldInfo = builder.setDatasetId(fieldExtractBO.getDatasetId())
                        .setName(fieldName).setType(fieldType).build();
                result.add(datasetFieldInfo);
            }

        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("ExtractDataset error: classNotFound", classNotFoundException);
        } catch (SQLException sqlException) {
            logger.error("ExtractDataset error: sqlException", sqlException);
        } finally {
            JDBCClient.close(statement, resultSet);
        }

        return result;
    }

    @Override
    public DatasetStatisticsInfo extractDatasetStatistics(DatasetStatisticsExtractBO statisticsExtractBO) {
        return null;
    }

    @Override
    public DatasetFieldStatisticsInfo extractDatasetFieldStatistics(DatasetFieldStatisticsExtractBO fieldStatisticsExtractBO) {
        return null;
    }
}
