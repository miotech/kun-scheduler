package com.miotech.kun.metadata.load.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.tool.DataStoreJsonUtil;
import com.miotech.kun.metadata.load.tool.DatasetGidGenerator;
import com.miotech.kun.metadata.load.tool.SnowflakeGigGenerator;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldPO;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

public class PostgresLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PostgresLoader.class);

    private final DatabaseOperator dbOperator;
    private DatasetGidGenerator gidGenerator;

    @Inject
    public PostgresLoader(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
        this.gidGenerator = new SnowflakeGigGenerator(dbOperator);
    }

    @Override
    public void load(Dataset dataset) {
        //TODO get gid
        long gid = gidGenerator.generate(dataset.getDataStore());

        boolean datasetExist = judgeDatasetExisted(gid);

        dbOperator.transaction(() -> {
            try {
                if (datasetExist) {
                    dbOperator.update("UPDATE kun_mt_dataset SET data_store = ?, `name` = ? WHERE gid = ?", DataStoreJsonUtil.toJson(dataset.getDataStore()), dataset.getName(), gid);
                } else {
                    dbOperator.update("INSERT INTO kun_mt_dataset(gid, `name`, cluster_id, data_store) VALUES(?, ?, ?, ?)", gid, dataset.getName(), dataset.getDataStore().getCluster().getClusterId(), DataStoreJsonUtil.toJson(dataset.getDataStore()));
                }

                dbOperator.update("INSERT INTO kun_mt_dataset_stats(dataset_gid, `row_count`, stats_date) VALUES (?, ?, ?)", gid, dataset.getDatasetStat().getRowCount(), dataset.getDatasetStat().getStatDate());

                Map<String, DatasetFieldPO> fieldInfos = new HashMap<>();
                List<String> deletedFields = new ArrayList<>();
                List<String> survivorFields = new ArrayList<>();
                fill(dataset.getFields(), fieldInfos, deletedFields, survivorFields, gid);

                Map<String, DatasetFieldStat> fieldStatMap = new HashMap<>();
                for (DatasetFieldStat fieldStat : dataset.getFieldStats()) {
                    fieldStatMap.put(fieldStat.getName(), fieldStat);
                }

                for (DatasetField datasetField : dataset.getFields()) {
                    long id = 0;
                    if (survivorFields.contains(datasetField.getName())) {
                        if (!fieldInfos.get(datasetField.getName()).getType().equals(datasetField.getType())) {
                            // update field type
                            dbOperator.update("UPDATE kun_mt_dataset_field SET `type` = ? WHERE gid = ? and `name` = ?", datasetField.getType(), gid, datasetField.getName());
                        }
                        id = fieldInfos.get(datasetField.getName()).getId();
                    } else {
                        // new field
                        String addFieldSql = "INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`) VALUES(?, ?, ?)";
                        id = dbOperator.create("INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`) VALUES(?, ?, ?)", gid, datasetField.getName(), datasetField.getType());
                    }

                    DatasetFieldStat datasetFieldStat = fieldStatMap.get(datasetField.getName());
                    BigDecimal nonnullPercentage = new BigDecimal("100.00");
                    if (dataset.getDatasetStat().getRowCount() > 0) {
                        nonnullPercentage = new BigDecimal(datasetFieldStat.getNonnullCount()).multiply(new BigDecimal("100")).divide(new BigDecimal(dataset.getDatasetStat().getRowCount()), 2, BigDecimal.ROUND_HALF_UP);
                    }
                    dbOperator.update("INSERT INTO kun_mt_dataset_field_stats(field_id, distinct_count, nonnull_count, nonnull_percentage) VALUES(?, ?, ?, ?)", id, datasetFieldStat.getDistinctCount(), datasetFieldStat.getNonnullCount(), nonnullPercentage);
                }
            } catch (JsonProcessingException jsonProcessingException) {
                throw new RuntimeException(jsonProcessingException);
            }
            return null;
        });

        /*Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.MYSQL, url, username, password);
            connection.setAutoCommit(false);

            // insert/update dataset
            boolean datasetExist = datasetExist(connection, gid);
            if (datasetExist) {
                // update
                String updateSchema = "UPDATE kun_mt_dataset SET data_store = ?, `name` = ? WHERE gid = ?";
                PreparedStatement updateSchemaStatement = connection.prepareStatement(updateSchema);
                updateSchemaStatement.setString(1, DataStoreJsonUtil.toJson(dataset.getDataStore()));
                updateSchemaStatement.setString(2, dataset.getName());
                updateSchemaStatement.setLong(3, gid);
                updateSchemaStatement.executeUpdate();
            } else {
                // insert
                String createSchema = "INSERT INTO kun_mt_dataset(gid, `name`, cluster_id, data_store) VALUES(?, ?, ?, ?)";
                PreparedStatement createSchemaStatement = connection.prepareStatement(createSchema);
                createSchemaStatement.setLong(1, gid);
                createSchemaStatement.setString(2, dataset.getName());
                createSchemaStatement.setLong(3, dataset.getDataStore().getCluster().getClusterId());
                createSchemaStatement.setString(4, DataStoreJsonUtil.toJson(dataset.getDataStore()));
                createSchemaStatement.executeUpdate();
            }

            // insert dataset stat info
            recordDatasetStat(connection, gid, dataset.getDatasetStat().getRowCount(), new Date(dataset.getDatasetStat().getStatDate().getTime()));

            Map<String, DatasetFieldPO> fieldInfos = new HashMap<>();
            List<String> deletedFields = new ArrayList<>();
            List<String> survivorFields = new ArrayList<>();
            fill(dataset.getFields(), fieldInfos, deletedFields, survivorFields, connection, gid);

            Map<String, DatasetFieldStat> fieldStatMap = new HashMap<>();
            for (DatasetFieldStat fieldStat : dataset.getFieldStats()) {
                fieldStatMap.put(fieldStat.getName(), fieldStat);
            }


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

                DatasetFieldStat datasetFieldStat = fieldStatMap.get(datasetField.getName());
                statement.setLong(2, datasetFieldStat.getDistinctCount());
                statement.setLong(3, datasetFieldStat.getNonnullCount());

                BigDecimal nonnullPercentage = new BigDecimal("100.00");
                if (dataset.getDatasetStat().getRowCount() > 0) {
                    nonnullPercentage = new BigDecimal(datasetFieldStat.getNonnullCount()).multiply(new BigDecimal("100")).divide(new BigDecimal(dataset.getDatasetStat().getRowCount()), 2, BigDecimal.ROUND_HALF_UP);
                }
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
        } catch (JsonProcessingException jsonProcessingException) {
            try {
                connection.rollback();
            } catch (SQLException rollbackSqlException) {}
            throw new RuntimeException(jsonProcessingException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }*/

    }

    private boolean judgeDatasetExisted(long gid) {
        Long c = dbOperator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset WHERE gid = ?", rs -> rs.getLong(1), gid);
        return (c != null && c != 0);
    }

    private void fill(List<DatasetField> fields, Map<String, DatasetFieldPO> fieldInfos, List<String> deletedFields,
                      List<String> survivorFields, long gid) {
        List<String> extractFields = fields.stream().map(field -> field.getName()).collect(Collectors.toList());

        // get all field name
        dbOperator.fetchOne("SELECT id, `name`, `type` FROM kun_mt_dataset_field WHERE dataset_gid = ?", (rs) -> {
            long id = rs.getLong(1);
            String fieldName = rs.getString(2);
            String fieldType = rs.getString(3);
            deletedFields.add(fieldName);
            survivorFields.add(fieldName);

            DatasetFieldPO fieldPO = new DatasetFieldPO(id, fieldType);
            fieldInfos.put(fieldName, fieldPO);
            return null;
        }, gid);
        deletedFields.removeAll(extractFields);
        survivorFields.retainAll(extractFields);
    }

}
