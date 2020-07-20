package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.model.ConfigurableDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.model.MetaStoreCatalog;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;

public class HiveDatabaseExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(HiveDatabaseExtractor.class);

    private final ConfigurableDataSource dataSource;

    private final String database;

    public HiveDatabaseExtractor(ConfigurableDataSource dataSource, String database) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        if (logger.isDebugEnabled()) {
            logger.debug("HiveDatabaseExtractor extract start. dataSource: {}, database: {}",
                    JSONUtils.toJsonString(dataSource), database);
        }

        List<String> tablesOnMySQL = extractTablesOnMySQL(database);

        if (logger.isDebugEnabled()) {
            logger.debug("HiveDatabaseExtractor extract end. tables: {}", JSONUtils.toJsonString(tablesOnMySQL));
        }
        return Iterators.concat(tablesOnMySQL.stream().map(tableName -> new HiveTableExtractor(dataSource, database, tableName).extract()).iterator());
    }

    private List<String> extractTablesOnMySQL(String database) {
        MetaStoreCatalog catalog = (MetaStoreCatalog) dataSource.getCatalog();
        DataSource dataSourceOfMySQL = JDBCClient.getDataSource(catalog.getUrl(), catalog.getUsername(),
                catalog.getPassword(), DatabaseType.MYSQL);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSourceOfMySQL);
        String showTables = "SELECT t.TBL_NAME FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID where d.NAME = ? AND d.CTLG_NAME = 'hive'";
        return dbOperator.fetchAll(showTables, rs -> rs.getString(1), database);
    }

}
