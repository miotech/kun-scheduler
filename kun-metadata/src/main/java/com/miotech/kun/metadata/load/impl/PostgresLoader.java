package com.miotech.kun.metadata.load.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.tool.DatasetGidGenerator;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostgresLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PostgresLoader.class);

    private String url;
    private String username;
    private String password;
    private DatasetGidGenerator gidGenerator;

    private PostgresLoader() {
    }

    public PostgresLoader(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void load(Dataset dataset) {
        //TODO get gid
        long gid = gidGenerator.generate(dataset.getDataStore());

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            JDBCClient.getConnection(DatabaseType.POSTGRES, url, username, password);
            connection.setAutoCommit(false);

            // insert/update dataset
            boolean datasetExist = datasetExist(connection, gid);
            if (datasetExist) {
                // update
            } else {
                // insert
            }

            // insert dataset stat info
            recordDatasetStat(connection, gid, dataset.getDatasetStat().getRowCount(), new Date(dataset.getDatasetStat().getStatDate().getTime()));

            Map<String, DatasetFieldPO> fieldInfos = new HashMap<>();
            List<String> deletedFields = new ArrayList<>();
            List<String> survivorFields = new ArrayList<>();
            fill(dataset.getFields(), fieldInfos, deletedFields, survivorFields, connection, gid);
            for (DatasetField datasetField : dataset.getFields()) {
                long id = 0;
                if (survivorFields.contains(datasetField.getName())) {
                    if (!fieldInfos.get(datasetField.getName()).getType().equals(datasetField.getType())) {
                        // update field type
                        String updateFieldTypeSql = "UPDATE kun_mt_dataset_field SET `type` = ? WHERE gid = ? and `name` = ?";
                        statement = connection.prepareStatement(updateFieldTypeSql);
                        statement.setString(1, datasetField.getType());
                        statement.setLong(2, gid);
                        statement.setString(3, datasetField.getName());
                        statement.executeUpdate();
                    }
                    id = fieldInfos.get(datasetField.getName()).getId();
                } else {
                    // new field
                    String addFieldSql = "INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`) VALUES(?, ?, ?)";
                    statement = connection.prepareStatement(addFieldSql, Statement.RETURN_GENERATED_KEYS);
                    statement.setLong(1, gid);
                    statement.setString(2, datasetField.getName());
                    statement.setString(3, datasetField.getType());
                    statement.executeUpdate();


                    resultSet = statement.getGeneratedKeys();
                    if (resultSet.next()) {
                        id = resultSet.getLong(1);
                    }
                }

                // insert field stat info
                String addFieldStatSql = "INSERT INTO kun_mt_dataset_field_stats(field_id, distinct_count, nonnull_count, nonnull_percentage) VALUES(?, ?, ?, ?)";
                statement = connection.prepareStatement(addFieldStatSql);
                statement.setLong(1, id);
                statement.setLong(2, datasetField.getDatasetFieldStat().getDistinctCount());
                statement.setLong(3, datasetField.getDatasetFieldStat().getNonnullCount());

                BigDecimal nonnullPercentage = new BigDecimal(datasetField.getDatasetFieldStat().getNonnullCount()).divide(new BigDecimal(dataset.getDatasetStat().getRowCount()));
                statement.setBigDecimal(4, nonnullPercentage);
                statement.executeUpdate();
            }

            connection.commit();
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            try {
                connection.rollback();
            } catch (SQLException sqlException) {}
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            try {
                connection.rollback();
            } catch (SQLException rollbackSqlException) {}
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

    }

    private void fill(List<DatasetField> fields, Map<String, DatasetFieldPO> fieldInfos, List<String> deletedFields,
                      List<String> survivorFields, Connection connection, long gid) throws SQLException {
        List<String> extractFields = fields.stream().map(field -> field.getName()).collect(Collectors.toList());

        // get all field name
        String getAllFieldNameSql = "SELECT id, `name`, `type` FROM kun_mt_dataset_field WHERE gid = ?";
        PreparedStatement statement = connection.prepareStatement(getAllFieldNameSql);
        statement.setLong(1, gid);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            long id = resultSet.getLong(0);
            String fieldName = resultSet.getString(1);
            String fieldType = resultSet.getString(2);
            deletedFields.add(fieldName);
            survivorFields.add(fieldName);

            DatasetFieldPO fieldPO = new DatasetFieldPO(id, fieldType);
            fieldInfos.put(fieldName, fieldPO);
        }

        deletedFields.removeAll(extractFields);
        survivorFields.retainAll(extractFields);
    }


    private void recordDatasetStat(Connection connection, long gid, long rowCount, Date statDate) throws SQLException {
        String datasetStatRecord = "INSERT INTO kun_mt_dataset_stats(dataset_gid, row_count, stats_date) VALUES (?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(datasetStatRecord);
        statement.setLong(1, gid);
        statement.setLong(2, rowCount);
        statement.setDate(3, statDate);
        statement.executeUpdate();
    }

    private boolean datasetExist(Connection connection, long gid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kun_mt_dataset WHERE gid = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, gid);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            long c = resultSet.getLong(0);
            return c != 0;
        }

        throw new SQLException("judge dataset exist error");
    }
}
