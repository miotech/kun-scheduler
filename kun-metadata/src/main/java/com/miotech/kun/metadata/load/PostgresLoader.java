package com.miotech.kun.metadata.load;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        long gid = 0L;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            JDBCClient.getConnection(DatabaseType.POSTGRES, url, username, password);
            connection.setAutoCommit(false);

            // insert/update dataset
            String sql = "SELECT COUNT(*) FROM kun_mt_dataset WHERE gid = ?";
            connection.prepareStatement(sql);
            statement.setLong(1, gid);
            resultSet = statement.executeQuery();

            boolean datasetExist = false;
            while (resultSet.next()) {
                long c = resultSet.getLong(0);
                datasetExist = c != 0;
            }
            if (datasetExist) {
                // update
            } else {
                // insert
            }

            // insert/update dataset stat info ?

            List<String> extractFields = dataset.getFields().stream().map(field -> field.getName()).collect(Collectors.toList());
            // get all field name
            // k:fieldName, v:fieldType
            Map<String, String> fieldInfos = new HashMap<>();
            List<String> deletedFields = new ArrayList<>();
            List<String> survivorFields = new ArrayList<>();
            String getAllFieldNameSql = "SELECT `name`, `type` FROM kun_mt_dataset_field WHERE gid = ?";
            connection.prepareStatement(getAllFieldNameSql);
            statement.setLong(1, gid);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String fieldName = resultSet.getString(0);
                String fieldType = resultSet.getString(1);
                deletedFields.add(fieldName);
                survivorFields.add(fieldName);

                fieldInfos.put(fieldName, fieldType);
            }


            deletedFields.removeAll(extractFields);
            survivorFields.retainAll(extractFields);

            for (DatasetField datasetField : dataset.getFields()) {
                if (survivorFields.contains(datasetField.getName())) {
                    if (!fieldInfos.get(datasetField.getName()).equals(datasetField.getType())) {
                        // update field type
                        String updateFieldTypeSql = "UPDATE kun_mt_dataset_field SET type = ? WHERE gid = ? and name = ?";

                    }
                } else {
                    // new field
                    String addFieldSql = "INSERT ...";

                }

                // update field stat info
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
}
