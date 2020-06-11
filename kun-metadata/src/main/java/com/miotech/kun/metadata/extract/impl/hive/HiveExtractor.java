package com.miotech.kun.metadata.extract.impl.hive;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.ConfigurableDataSource;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.MetaStoreCatalog;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;

public class HiveExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private final ConfigurableDataSource dataSource;

    public HiveExtractor(ConfigurableDataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {
        logger.debug("HiveExtractor extract start. dataSource: {}", JSONUtils.toJsonString(dataSource));
        List<String> databases = extractDatabasesOnMySQL();
        logger.debug("HiveExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
        return Iterators.concat(databases.stream().map((databasesName) -> new HiveDatabaseExtractor(dataSource, databasesName).extract()).iterator());
    }

    private List<String> extractDatabasesOnMySQL() {
        MetaStoreCatalog catalog = (MetaStoreCatalog) dataSource.getCatalog();
        DataSource dataSourceOfMySQL = JDBCClient.getDataSource(catalog.getUrl(), catalog.getUsername(),
                catalog.getPassword(), DatabaseType.MYSQL);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSourceOfMySQL);

        String showDatabases = "SELECT d.NAME FROM DBS d WHERE CTLG_NAME = 'hive'";
        return dbOperator.fetchAll(showDatabases, rs -> rs.getString(1));
    }

}
