package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.beust.jcommander.internal.Lists;
import com.google.common.annotations.VisibleForTesting;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.context.ApplicationContext;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import com.miotech.kun.metadata.databuilder.service.fieldmapping.FieldMappingService;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class HiveTableSchemaExtractor extends SchemaExtractorTemplate {

    private final String dbName;
    private final String tableName;
    private final HiveDataSource hiveDataSource;
    private final FieldMappingService fieldMappingService;

    public HiveTableSchemaExtractor(HiveDataSource hiveDataSource, String dbName, String tableName) {
        super(hiveDataSource.getId());
        this.dbName = dbName;
        this.tableName = tableName;
        this.hiveDataSource = hiveDataSource;
        this.fieldMappingService = ApplicationContext.getContext().getInjector().getInstance(FieldMappingService.class);
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JDBCClient.getConnection(hiveDataSource.getMetastoreUrl(), hiveDataSource.getMetastoreUsername(),
                    hiveDataSource.getMetastorePassword(), DatabaseType.MYSQL);
            String schemas = "SELECT source.* FROM  " +
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
            statement = connection.prepareStatement(schemas);
            statement.setString(1, tableName);
            statement.setString(2, tableName);

            resultSet = statement.executeQuery();
            List<DatasetField> fields = Lists.newArrayList();
            while (resultSet.next()) {
                String name = resultSet.getString(6);
                String rawType = resultSet.getString(8);
                String description = resultSet.getString(9);
                fields.add(new DatasetField(name, new DatasetFieldType(fieldMappingService.parse(hiveDataSource.getType().name(), rawType), rawType), description));
            }

            return fields;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(hiveDataSource.getDatastoreUrl(), dbName, tableName);
    }

    @Override
    protected String getName() {
        return tableName;
    }

    @Override
    protected void close() {

    }

}