package com.miotech.kun.metadata.extract.impl.postgres;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.extract.impl.hive.HiveExtractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        List<String> databases = Lists.newArrayList();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, dataSource.getUrl(),
                    dataSource.getUsername(), dataSource.getPassword());
            String scanDatabase = "SELECT datname FROM pg_database WHERE datistemplate = FALSE";
            statement = connection.prepareStatement(scanDatabase);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                if (!filterDatabase.contains(databaseName)) {
                    databases.add(databaseName);
                }
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw ExceptionUtils.wrapIfChecked(classNotFoundException);
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        logger.debug("PostgresExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
        return Iterators.concat(databases.stream().map((databasesName) -> new PostgresDatabaseExtractor(dataSource, databasesName).extract()).iterator());
    }

}
