package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.databuilder.client.MetaStoreClient;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;

import java.util.Iterator;
import java.util.List;

public class HiveSchemaExtractor extends HiveExistenceExtractor implements DatasetSchemaExtractor {

    @Override
    public List<DatasetField> extract(Dataset dataset, DataSource dataSource) {
        HiveTableSchemaExtractor hiveTableSchemaExtractor = null;
        try {
            HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
            hiveTableSchemaExtractor = new HiveTableSchemaExtractor(hiveDataSource, dataset.getDatabaseName(), dataset.getName());
            return hiveTableSchemaExtractor.getSchema();
        } finally {
            if (hiveTableSchemaExtractor != null) {
                hiveTableSchemaExtractor.close();
            }
        }
    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        HiveDataSource hiveDataSource = (HiveDataSource) dataSource;
        HiveMetaStoreClient client = null;
        try {
            client = MetaStoreClient.getClient(hiveDataSource.getMetaStoreUris());
            List<String> databases = client.getAllDatabases();

            return Iterators.concat(databases.stream().map(databasesName -> new HiveDatabaseSchemaExtractor(hiveDataSource, databasesName).extract()).iterator());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            MetaStoreClient.close(client);
        }
    }
}
