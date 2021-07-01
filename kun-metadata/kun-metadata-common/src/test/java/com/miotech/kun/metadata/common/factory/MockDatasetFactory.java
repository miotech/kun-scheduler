package com.miotech.kun.metadata.common.factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.workflow.core.model.lineage.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

public class MockDatasetFactory {

    private MockDatasetFactory() {
    }

    public static Dataset createDatasetWithFields(long dataSourceId, List<DatasetField> fields) {
        String random = UUID.randomUUID().toString();
        return createDatasetWithName(dataSourceId, "dataset:" + random, fields);
    }

    public static Dataset createDatasetWithName(long dataSourceId, String name, List<DatasetField> fields) {
        return createDataset(IdGenerator.getInstance().nextId(), name, dataSourceId, "db", fields, "Hive");
    }

    public static Dataset createDataset(String name, long dataSourceId, String databaseName, List<DatasetField> fields, String dataStoreType) {
        long gid = IdGenerator.getInstance().nextId();
        return createDataset(gid, name, dataSourceId, databaseName, fields, dataStoreType);
    }

    public static Dataset createDataset(long gid) {
        String random = UUID.randomUUID().toString();
        return createDataset(gid, "dataset:" + random);
    }

    public static Dataset createDataset(long gid, String name) {
        return createDataset(gid, name, 1L, "default", "Hive");
    }

    public static Dataset createDataset(long gid, String name, long dataSourceId, String databaseName, String dataStoreType) {
        return createDataset(gid, name, dataSourceId, databaseName, Lists.newArrayList(), dataStoreType);
    }

    public static Dataset createDataset(String name) {
        long gid = IdGenerator.getInstance().nextId();
        return createDataset(gid, name);
    }

    public static Dataset createDatasetWithDataSourceId(long dataSourceId) {
        return createDatasetWithFields(dataSourceId, Lists.newArrayList());
    }

    public static Dataset createDataset(long gid, String name, long dataSourceId, String databaseName, List<DatasetField> fields, String dataStoreType) {
        DataStore dataStore = createDataStore(dataStoreType, databaseName);
        return Dataset.newBuilder()
                .withGid(gid)
                .withDatasourceId(dataSourceId)
                .withName(name)
                .withDataStore(dataStore)
                .withFields(fields)
                .withDeleted(false)
                .build();
    }

    private static DataStore createDataStore(String dataStoreType, String databaseName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(dataStoreType), "Param `dataStoreType` should not be empty");

        switch (dataStoreType) {
            case "Hive":
                return new HiveTableStore("location", databaseName, "table");
            case "Mongo":
                return new MongoDataStore("127.0.0.1", 27017, databaseName, "collection");
            case "Postgres":
                return new PostgresDataStore("127.0.0.1", 5432, databaseName, "public", "table");
            case "Arango":
                return new ArangoCollectionStore("127.0.0.1", 8529, databaseName, "collection");
            case "Elasticsearch":
                return new ElasticSearchIndexStore("127.0.0.1", 9200, "index");
            default:
                throw new IllegalArgumentException("Invalid dataStoreType: " + dataStoreType);
        }
    }



}
