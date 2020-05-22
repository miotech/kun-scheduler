package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.DatasetFieldExtractor;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HiveDatasetFieldExtractor implements DatasetFieldExtractor {
    private static Logger logger = LoggerFactory.getLogger(HiveDatasetFieldExtractor.class);
    private DatasetFieldRequest fieldRequest;

    public HiveDatasetFieldExtractor(DatasetFieldRequest fieldRequest) {
        this.fieldRequest = fieldRequest;
    }

    @Override
    public List<DatasetField> extract() {
        List<DatasetField> fields = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.MYSQL, fieldRequest.getMetaStoreUrl(),
                    fieldRequest.getMetaStoreUsername(), fieldRequest.getMetaStorePassword());
            String sql = "SELECT source.* FROM  " +
                    "    (SELECT t.TBL_ID, d.NAME as `schema`, t.TBL_NAME name, t.TBL_TYPE, tp.PARAM_VALUE as description,  " +
                    "           p.PKEY_NAME as col_name, p.INTEGER_IDX as col_sort_order,  " +
                    "           p.PKEY_TYPE as col_type, p.PKEY_COMMENT as col_description, 1 as is_partition_col,  " +
                    "           IF(t.TBL_TYPE = 'VIRTUAL_VIEW', 1, 0) is_view " +
                    "    FROM TBLS t" +
                    "    JOIN DBS d ON t.DB_ID = d.DB_ID" +
                    "    JOIN PARTITION_KEYS p ON t.TBL_ID = p.TBL_ID " +
                    "    LEFT JOIN TABLE_PARAMS tp ON (t.TBL_ID = tp.TBL_ID AND tp.PARAM_KEY='comment') " +
                    "    WHERE t.TBL_NAME = ? " +
                    "    UNION " +
                    "    SELECT t.TBL_ID, d.NAME as `schema`, t.TBL_NAME name, t.TBL_TYPE, tp.PARAM_VALUE as description, " +
                    "           c.COLUMN_NAME as col_name, c.INTEGER_IDX as col_sort_order, " +
                    "           c.TYPE_NAME as col_type, c.COMMENT as col_description, 0 as is_partition_col, " +
                    "           IF(t.TBL_TYPE = 'VIRTUAL_VIEW', 1, 0) is_view " +
                    "    FROM TBLS t " +
                    "    JOIN DBS d ON t.DB_ID = d.DB_ID " +
                    "    JOIN SDS s ON t.SD_ID = s.SD_ID " +
                    "    JOIN COLUMNS_V2 c ON s.CD_ID = c.CD_ID " +
                    "    LEFT JOIN TABLE_PARAMS tp ON (t.TBL_ID = tp.TBL_ID AND tp.PARAM_KEY='comment') " +
                    "    WHERE t.TBL_NAME = ? " +
                    "    ) source " +
                    "    ORDER by tbl_id, is_partition_col desc;";
            statement = connection.prepareStatement(sql);

            statement.setString(1, fieldRequest.getTable());
            statement.setString(2, fieldRequest.getTable());
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString(5);
                String type = resultSet.getString(7);
                String description = resultSet.getString(8);

                DatasetField field = new DatasetField(name, type, description);
                fields.add(field);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return fields;
    }
}
