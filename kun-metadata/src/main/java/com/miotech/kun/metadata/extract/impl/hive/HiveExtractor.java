package com.miotech.kun.metadata.extract.impl.hive;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.Database;
import com.amazonaws.services.glue.model.GetDatabasesRequest;
import com.amazonaws.services.glue.model.GetDatabasesResult;
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
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class HiveExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private final HiveCluster cluster;

    public HiveExtractor(HiveCluster cluster) {
        Preconditions.checkNotNull(cluster, "cluster should not be null.");
        this.cluster = cluster;
    }

    @Override
    public Iterator<Dataset> extract() {
        logger.debug("HiveExtractor extract start. cluster: {}", JSONUtils.toJsonString(cluster));
        List<String> databases;
        switch (cluster.getMetaStoreType()) {
            case MYSQL:
                databases = extractDatabasesOnMySQL();
                break;
            case GLUE:
                databases = extractDatabasesOnGlue();
                break;
            default:
                throw new RuntimeException("invalid metaStoreType: " + cluster.getMetaStoreType());
        }

        logger.debug("HiveExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
        return Iterators.concat(databases.stream().map((databasesName) -> new HiveDatabaseExtractor(cluster, databasesName).extract()).iterator());
    }

    private List<String> extractDatabasesOnGlue() {
        List<String> databasesOnGlue = Lists.newArrayList();
        AWSGlue awsGlueClient = GlueClient.getAWSGlue(cluster.getMetaStoreUsername(),
                cluster.getMetaStorePassword(), cluster.getMetaStoreUrl());
        GetDatabasesRequest getDatabasesRequest = new GetDatabasesRequest();
        GetDatabasesResult databasesResult = awsGlueClient.getDatabases(getDatabasesRequest);
        if (databasesResult != null && databasesResult.getDatabaseList() != null) {
            for (Database database : databasesResult.getDatabaseList()) {
                databasesOnGlue.add(database.getName());
            }
        }
        return databasesOnGlue;
    }

    private List<String> extractDatabasesOnMySQL() {
        DataSource dataSourceOfMySQL = JDBCClient.getDataSource(cluster.getMetaStoreUrl(),
                cluster.getMetaStoreUsername(), cluster.getMetaStorePassword(), DatabaseType.MYSQL);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSourceOfMySQL);

        String scanDatabase = "SELECT d.NAME FROM DBS d WHERE CTLG_NAME = 'hive'";
        return dbOperator.fetchAll(scanDatabase, rs -> rs.getString(1));
    }

}
