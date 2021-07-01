package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Singleton
public class DatasetFieldMappingDao {

    private static final Logger logger = LoggerFactory.getLogger(DatasetFieldMappingDao.class);

    private static final String[] FIELD_MAPPING_COLUMNS = { "datasource_type", "pattern", "type" };
    private static final String DATASET_FIELD_MAPPING_TABLE_NAME = "kun_mt_dataset_field_mapping";

    @Inject
    private DatabaseOperator dbOperator;

    public List<DatasetFieldMapping> fetchByDatasourceType(String datasourceType) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(FIELD_MAPPING_COLUMNS)
                .from(DATASET_FIELD_MAPPING_TABLE_NAME)
                .where("datasource_type = ?")
                .getSQL();

        logger.debug("Fetch field mapping with datasourceType: {}", datasourceType);
        logger.debug("Fetch field mapping by datasourceType, sql: {}", sql);
        return dbOperator.fetchAll(sql, DatasetFieldMappingMapper.INSTANCE, datasourceType);
    }

    private static class DatasetFieldMappingMapper implements ResultSetMapper<DatasetFieldMapping> {
        public static final ResultSetMapper<DatasetFieldMapping> INSTANCE = new DatasetFieldMappingMapper();

        @Override
        public DatasetFieldMapping map(ResultSet rs) throws SQLException {
            return new DatasetFieldMapping(rs.getString("datasource_type"),
                    rs.getString("pattern"), rs.getString("type"));
        }
    }

}
