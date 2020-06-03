package com.miotech.kun.metadata.extract.impl.hive;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.SearchTablesRequest;
import com.amazonaws.services.glue.model.SearchTablesResult;
import com.amazonaws.services.glue.model.Table;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.client.GlueClient;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.HiveCluster;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class HiveDatabaseExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(HiveDatabaseExtractor.class);

    private final HiveCluster cluster;
    private final String database;

    public HiveDatabaseExtractor(HiveCluster cluster, String database) {
        Preconditions.checkNotNull(cluster, "cluster should not be null.");
        this.cluster = cluster;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        logger.debug("HiveDatabaseExtractor extract start. cluster: {}, database: {}", JSONUtils.toJsonString(cluster), database);
        switch (cluster.getMetaStoreType()) {
            case MYSQL:
                List<String> tables = Lists.newArrayList();
                Connection connection = null;
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    connection = JDBCClient.getConnection(DatabaseType.MYSQL, cluster.getMetaStoreUrl(),
                            cluster.getMetaStoreUsername(), cluster.getMetaStorePassword());
                    String scanDatabase = "SELECT t.TBL_NAME FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID where d.NAME = ? AND d.CTLG_NAME = 'hive'";
                    statement = connection.prepareStatement(scanDatabase);

                    statement.setString(1, database);
                    resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String tableName = resultSet.getString(1);
                        tables.add(tableName);
                    }
                } catch (ClassNotFoundException classNotFoundException) {
                    logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
                    throw ExceptionUtils.wrapIfChecked(classNotFoundException);
                } catch (SQLException sqlException) {
                    throw ExceptionUtils.wrapIfChecked(sqlException);
                } finally {
                    JDBCClient.close(connection, statement, resultSet);
                }
                logger.debug("HiveDatabaseExtractor extract end. tables: {}", JSONUtils.toJsonString(tables));
                return Iterators.concat(tables.stream().map((tableName) -> new HiveTableExtractor(cluster, database, tableName, null).extract()).iterator());
            case GLUE:
                List<Table> glueTables = Lists.newArrayList();
                AWSGlue awsGlueClient = GlueClient.getAWSGlue(cluster.getMetaStoreUsername(),
                        cluster.getMetaStorePassword(), cluster.getMetaStoreUrl());

                String nextToken = null;
                SearchTablesResult searchTablesResult;
                do {
                    SearchTablesRequest searchTablesRequest = new SearchTablesRequest();
                    searchTablesRequest.withNextToken(nextToken);

                    searchTablesResult = awsGlueClient.searchTables(searchTablesRequest);
                    if (searchTablesResult != null) {
                        if (searchTablesResult.getTableList() != null) {
                            for (Table table : searchTablesResult.getTableList()) {
                                if (database.equals(table.getDatabaseName()) && filterIgnoredTables(table))
                                glueTables.add(table);
                            }
                        }
                        nextToken = searchTablesResult.getNextToken();
                    }
                } while (searchTablesResult.getNextToken() != null);
                logger.debug("HiveDatabaseExtractor extract end. tablesSize: {}", glueTables.size());
                return Iterators.concat(glueTables.stream().map((table) -> new HiveTableExtractor(cluster, database, table.getName(), table).extract()).iterator());
            default:
                throw new RuntimeException("invalid metaStoreType: " + cluster.getMetaStoreType());
        }
    }

    private boolean filterIgnoredTables(Table table) {
        if (!(table.getName().startsWith("tmp") ||
                table.getName().endsWith("_last") ||
                table.getName().endsWith("_temp") ||
                table.getName().endsWith("_dev") ||
                table.getName().endsWith("_tmp"))) {
            return true;
        }

        return false;
    }

}
