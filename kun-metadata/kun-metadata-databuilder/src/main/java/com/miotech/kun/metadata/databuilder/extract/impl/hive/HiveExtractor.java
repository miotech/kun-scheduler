package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;

public class HiveExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private final HiveDataSource dataSource;

    public HiveExtractor(HiveDataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {
        if (logger.isDebugEnabled()) {
            logger.debug("HiveExtractor extract start. dataSource: {}", JSONUtils.toJsonString(dataSource));
        }

        List<String> databases = extractDatabasesOnMySQL();
        if (logger.isDebugEnabled()) {
            logger.debug("HiveExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
        }
        return Iterators.concat(databases.stream().map(databasesName -> new HiveDatabaseExtractor(dataSource, databasesName).extract()).iterator());
    }

    private List<String> extractDatabasesOnMySQL() {
        DataSource dataSourceOfMySQL = JDBCClient.getDataSource(dataSource.getMetastoreUrl(), dataSource.getMetastoreUsername(),
                dataSource.getMetastorePassword(), DatabaseType.MYSQL);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSourceOfMySQL);

        String showDatabases = "SELECT d.NAME FROM DBS d WHERE CTLG_NAME = 'hive'";
        return dbOperator.fetchAll(showDatabases, rs -> rs.getString(1));
    }

}
