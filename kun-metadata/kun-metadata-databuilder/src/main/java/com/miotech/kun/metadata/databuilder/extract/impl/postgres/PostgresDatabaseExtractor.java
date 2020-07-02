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

public class PostgresDatabaseExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(PostgresDatabaseExtractor.class);

    private final PostgresDataSource dataSource;
    private final String database;

    public PostgresDatabaseExtractor(PostgresDataSource dataSource, String database) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresDatabaseExtractor extract start. dataSource: {}, database: {}",
                    JSONUtils.toJsonString(dataSource), database);
        }

        DataSource pgDataSource = JDBCClient.getDataSource(UseDatabaseUtil.useDatabase(dataSource.getUrl(), database), this.dataSource.getUsername(), this.dataSource.getPassword(), DatabaseType.POSTGRES);
        DatabaseOperator dbOperator = new DatabaseOperator(pgDataSource);
        String showSchemas = "SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%' AND schema_name != 'information_schema'";
        List<String> schemas = dbOperator.fetchAll(showSchemas, rs -> rs.getString(1));

        if (logger.isDebugEnabled()) {
            logger.debug("PostgresDatabaseExtractor extract end. schemas: {}", JSONUtils.toJsonString(schemas));
        }
        return Iterators.concat(schemas.stream().map((schema) -> new PostgresSchemaExtractor(dataSource, database, schema).extract()).iterator());
    }

}
