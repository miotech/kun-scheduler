package com.miotech.kun.metadata.databuilder.extract.impl.postgres;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;

public class PostgresExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(PostgresExtractor.class);

    private final PostgresDataSource dataSource;

    public PostgresExtractor(PostgresDataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresExtractor extract start. dataSource: {}", JSONUtils.toJsonString(dataSource));
        }

        DataSource pgDataSource = JDBCClient.getDataSource(this.dataSource.getUrl(), this.dataSource.getUsername(), this.dataSource.getPassword(), DatabaseType.POSTGRES);
        DatabaseOperator dbOperator = new DatabaseOperator(pgDataSource);
        String showDatabases = "SELECT datname FROM pg_database WHERE datistemplate = FALSE";
        List<String> databases = dbOperator.fetchAll(showDatabases, rs -> rs.getString(1));

        if (logger.isDebugEnabled()) {
            logger.debug("PostgresExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
        }
        return Iterators.concat(databases.stream().map((databasesName) -> new PostgresDatabaseExtractor(dataSource, databasesName).extract()).iterator());
    }

}
