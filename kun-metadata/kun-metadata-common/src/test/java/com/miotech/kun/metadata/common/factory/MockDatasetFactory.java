package com.miotech.kun.metadata.common.factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.vo.DatasetUpdateRequest;
import com.miotech.kun.workflow.core.model.lineage.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MockDatasetFactory {

    private MockDatasetFactory() {
    }

    public static Dataset createDatasetWithFields(long dataSourceId, List<DatasetField> fields) {
        String random = UUID.randomUUID().toString();
        return createDatasetWithName(dataSourceId, "dataset:" + random, fields);
    }

    public static Dataset createDatasetWithDatabase(long dataSourceId, String name) {
        String random = UUID.randomUUID().toString();
        return createDataset(IdGenerator.getInstance().nextId(), "dataset:" + random, dataSourceId, name, null, DataStoreType.HIVE_TABLE);
    }

    public static Dataset createDatasetWithDatabase(long dataSourceId, String datasetName, String databaseName) {
        String random = UUID.randomUUID().toString();
        return createDataset(IdGenerator.getInstance().nextId(), datasetName, dataSourceId, databaseName, null, DataStoreType.HIVE_TABLE);
    }

    public static Dataset createDatasetWithName(long dataSourceId, String name, List<DatasetField> fields) {
        return createDataset(IdGenerator.getInstance().nextId(), name, dataSourceId, "db", fields, DataStoreType.HIVE_TABLE);
    }

    public static Dataset createDataset(String name, long dataSourceId, String databaseName, List<DatasetField> fields, DataStoreType dataStoreType) {
        long gid = IdGenerator.getInstance().nextId();
        return createDataset(gid, name, dataSourceId, databaseName, fields, dataStoreType);
    }

    public static Dataset createDataset(long gid) {
        String random = UUID.randomUUID().toString();
        return createDataset(gid, "dataset:" + random);
    }

    public static Dataset createDataset(long gid, String name) {
        return createDataset(gid, name, 1L, "default", DataStoreType.HIVE_TABLE);
    }

    public static Dataset createDataset(long gid, String name, long dataSourceId, String databaseName, DataStoreType dataStoreType) {
        return createDataset(gid, name, dataSourceId, databaseName, Lists.newArrayList(), dataStoreType);
    }

    public static Dataset createDataset(String name, long dataSourceId, String databaseName, DataStoreType dataStoreType) {
        Long gid = IdGenerator.getInstance().nextId();
        return createDataset(gid, name, dataSourceId, databaseName, Lists.newArrayList(), dataStoreType);
    }

    public static Dataset createDataset(String name) {
        long gid = IdGenerator.getInstance().nextId();
        return createDataset(gid, name);
    }

    public static DatasetUpdateRequest createDatasetUpdateRequest(Long gid, ArrayList<String> owners, ArrayList<String> tags) {
        return new DatasetUpdateRequest("test" + gid, owners, tags);
    }


    public static Dataset createDatasetWithDataSourceId(long dataSourceId) {
        return createDatasetWithFields(dataSourceId, Lists.newArrayList());
    }

    public static Dataset createDataset(long gid, String name, long dataSourceId, String databaseName, List<DatasetField> fields, DataStoreType dataStoreType) {
        DataStore dataStore = createDataStore(dataStoreType, databaseName, name);
        return Dataset.newBuilder()
                .withGid(gid)
                .withDatasourceId(dataSourceId)
                .withName(name)
                .withDataStore(dataStore)
                .withFields(fields)
                .withDeleted(false)
                .build();
    }

    public static Dataset createDatasetWithDataStore(long gid, String name, long dataSourceId, List<DatasetField> fields, DataStore dataStore) {
        return Dataset.newBuilder()
                .withGid(gid)
                .withDatasourceId(dataSourceId)
                .withName(name)
                .withDataStore(dataStore)
                .withFields(fields)
                .withDeleted(false)
                .build();
    }

    public static DataStore createDataStore(DataStoreType dataStoreType, String databaseName, String tableName) {
        switch (dataStoreType) {
            case HIVE_TABLE:
                return new HiveTableStore("location", databaseName, tableName);
            case MONGO_COLLECTION:
                return new MongoDataStore("127.0.0.1", 27017, databaseName, tableName);
            case POSTGRES_TABLE:
                return new PostgresDataStore("127.0.0.1", 5432, databaseName, "public", tableName);
            case ARANGO_COLLECTION:
                return new ArangoCollectionStore("127.0.0.1", 8529, databaseName, tableName);
            case ELASTICSEARCH_INDEX:
                return new ElasticSearchIndexStore("127.0.0.1", 9200, tableName);
            default:
                throw new IllegalArgumentException("Invalid dataStoreType: " + dataStoreType);
        }
    }


}
