package com.miotech.kun.metadata.extract.impl.hive;

import com.amazonaws.services.glue.model.Table;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.extract.iterator.GlueTableIterator;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.HiveCluster;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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
                List<String> tablesOnMySQL = extractTablesOnMySQL(database);
                logger.debug("HiveDatabaseExtractor extract end. tables: {}", JSONUtils.toJsonString(tablesOnMySQL));
                return Iterators.concat(tablesOnMySQL.stream().map((tableName) -> new HiveTableExtractor(cluster, database, tableName, null).extract()).iterator());
            case GLUE:
                GlueTableIterator glueTableIterator = new GlueTableIterator(database, cluster.getMetaStoreUsername(),
                        cluster.getMetaStorePassword(), cluster.getMetaStoreUrl());
                while (glueTableIterator.hasNext()) {
                    List<Table> tablesOnGlue = glueTableIterator.next();
                    logger.debug("HiveDatabaseExtractor extract end. tablesSize: {}", tablesOnGlue.size());
                    return Iterators.concat(tablesOnGlue.stream().map((table) -> new HiveTableExtractor(cluster, database, table.getName(), table).extract()).iterator());
                }
            default:
                throw new RuntimeException("invalid metaStoreType: " + cluster.getMetaStoreType());
        }
    }

    private List<String> extractTablesOnMySQL(String database) {
        DataSource dataSourceOfMySQL = JDBCClient.getDataSource(cluster.getMetaStoreUrl(),
                cluster.getMetaStoreUsername(), cluster.getMetaStorePassword(), DatabaseType.MYSQL);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSourceOfMySQL);
        String scanDatabase = "SELECT t.TBL_NAME FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID where d.NAME = ? AND d.CTLG_NAME = 'hive'";
        return dbOperator.fetchAll(scanDatabase, rs -> rs.getString(1), database);
    }

}
