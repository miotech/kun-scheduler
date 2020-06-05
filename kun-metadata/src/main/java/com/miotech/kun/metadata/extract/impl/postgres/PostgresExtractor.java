package com.miotech.kun.metadata.extract.impl.postgres;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.extract.impl.hive.HiveExtractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresDataSource;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;

public class PostgresExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private final PostgresDataSource dataSource;

    private static final List<String> filterDatabase;

    static {
        filterDatabase = ImmutableList.of("postgres", "agens", "mdp_test", "northwind");
    }

    public PostgresExtractor(PostgresDataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {
        logger.debug("PostgresExtractor extract start. dataSource: {}", JSONUtils.toJsonString(dataSource));
        DataSource pgDataSource = JDBCClient.getDataSource(this.dataSource.getUrl(), this.dataSource.getUsername(), this.dataSource.getPassword(), DatabaseType.POSTGRES);
        DatabaseOperator dbOperator = new DatabaseOperator(pgDataSource);
        String showDatabases = "SELECT datname FROM pg_database WHERE datistemplate = FALSE";
        List<String> databases = dbOperator.fetchAll(showDatabases, rs -> rs.getString(1));

        logger.debug("PostgresExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
        return Iterators.concat(databases.stream().map((databasesName) -> new PostgresDatabaseExtractor(dataSource, databasesName).extract()).iterator());
    }

}
