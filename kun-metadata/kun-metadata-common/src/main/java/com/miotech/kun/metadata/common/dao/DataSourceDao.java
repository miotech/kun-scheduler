package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class DataSourceDao {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceDao.class);

    private static final String METADATA_TAG_TABLE_NAME = "kun_mt_tag";
    private static final String DATASOURCE_TABLE_NAME = "kun_mt_datasource";
    private static final String DATASOURCE_ATTR_TABLE_NAME = "kun_mt_datasource_attrs";
    private static final String DATASOURCE_TAG_TABLE_NAME = "kun_mt_datasource_tags";
    private static final String DATASOURCE_TYPE_TABLE_NAME = "kun_mt_datasource_type";
    private static final String DATASOURCE_TYPE_FIELD_TABLE_NAME = "kun_mt_datasource_type_fields";

    public static final String DATASOURCE_MODEL_NAME = "datasource";
    public static final String DATASOURCE_ATTR_MODEL_NAME = "datasource_attrs";
    public static final String DATASOURCE_TYPE_MODEL_NAME = "datasource_type";
    public static final String DATASOURCE_TYPE_FIELD_MODEL_NAME = "datasource_type_field";

    private static final String[] DATASOURCE_TABLE_COLUMNS = {"id", "connection_info", "type_id"};
    private static final String[] DATASOURCE_ATTR_TABLE_COLUMNS = {"datasource_id", "name", "create_user", "create_time", "update_user", "update_time"};
    private static final String[] DATASOURCE_TAG_TABLE_COLUMNS = {"datasource_id", "tag"};
    private static final String[] DATASOURCE_TYPE_TABLE_COLUMNS = {"id", "name"};
    private static final String[] DATASOURCE_TYPE_FIELD_TABLE_COLUMNS = {"id", "type_id", "name", "sequence_order", "format", "require"};

    @Inject
    DatabaseOperator dbOperator;

    public List<Long> fetchDataSourceIdByType(String typeName) {
        logger.debug("Fetch dataSourceId with type: {}", typeName);
        String dataSourceIdSQL = DefaultSQLBuilder.newBuilder()
                .select(DATASOURCE_MODEL_NAME + ".id")
                .from(DATASOURCE_TABLE_NAME, DATASOURCE_MODEL_NAME)
                .join("INNER", DATASOURCE_TYPE_TABLE_NAME, DATASOURCE_TYPE_MODEL_NAME)
                .on(DATASOURCE_MODEL_NAME + ".type_id = " + DATASOURCE_TYPE_MODEL_NAME + ".id")
                .where(DATASOURCE_TYPE_MODEL_NAME + ".name = ?")
                .getSQL();

        return dbOperator.fetchAll(dataSourceIdSQL, rs -> rs.getLong(1), typeName);
    }

}
