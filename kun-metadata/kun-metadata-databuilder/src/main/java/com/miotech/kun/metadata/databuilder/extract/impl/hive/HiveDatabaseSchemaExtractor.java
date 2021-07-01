package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.client.MetaStoreClient;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;

import java.util.Iterator;
import java.util.List;

public class HiveDatabaseSchemaExtractor implements Extractor {
    private final HiveDataSource hiveDataSource;

    private final String dbName;

    public HiveDatabaseSchemaExtractor(HiveDataSource hiveDataSource, String dbName) {
        Preconditions.checkNotNull(hiveDataSource, "dataSource should not be null.");
        this.hiveDataSource = hiveDataSource;
        this.dbName = dbName;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> tablesOnMySQL = extractTables(dbName);
        return Iterators.concat(tablesOnMySQL.stream().map(tableName -> new HiveTableSchemaExtractor(hiveDataSource, dbName, tableName).extract()).iterator());
    }

    private List<String> extractTables(String dbName) {
        HiveMetaStoreClient client = null;

        try {
            client = MetaStoreClient.getClient(hiveDataSource.getMetaStoreUris());
            return client.getAllTables(dbName);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            MetaStoreClient.close(client);
        }
    }

}
