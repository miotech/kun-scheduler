package com.miotech.kun.metadata.databuilder.extract.impl.postgres;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;

public class PostgresSchemaExtractor implements Extractor {

    private static Logger logger = LoggerFactory.getLogger(PostgresSchemaExtractor.class);

    private final PostgresDataSource dataSource;
    private final String database;
    private final String schema;

    public PostgresSchemaExtractor(PostgresDataSource dataSource, String database, String schema) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
        this.schema = schema;
    }

    @Override
    public Iterator<Dataset> extract() {
        DataSource pgDataSource = JDBCClient.getDataSource(UseDatabaseUtil.useSchema(dataSource.getUrl(), database, schema), this.dataSource.getUsername(), this.dataSource.getPassword(), DatabaseType.POSTGRES);
        DatabaseOperator dbOperator = new DatabaseOperator(pgDataSource);
        String showTables = "SELECT tablename FROM pg_tables WHERE schemaname = ?";
        List<String> tables = dbOperator.fetchAll(showTables, rs -> rs.getString(1), schema);

        if (logger.isDebugEnabled()) {
            logger.debug("PostgresSchemaExtractor extract end. tables: {}", JSONUtils.toJsonString(tables));
        }
        return Iterators.concat(tables.stream().map((table) -> new PostgresTableExtractor(dataSource, database, schema, table, dbOperator).extract()).iterator());
    }
}
