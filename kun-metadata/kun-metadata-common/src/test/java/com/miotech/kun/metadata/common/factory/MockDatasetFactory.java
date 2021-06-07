package com.miotech.kun.metadata.common.factory;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.workflow.core.model.lineage.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MockDatasetFactory {

    private MockDatasetFactory() {
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
        if (StringUtils.isBlank(dataStoreType)) {
            throw new IllegalArgumentException("Invalid dataStoreType: " + dataStoreType);
        }

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

    public static void insertDataset(DatabaseOperator dbOperator, Dataset dataset) {
        dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES(?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                dataset.getGid(), dataset.getName(), dataset.getDatasourceId(), DataStoreJsonUtil.toJson(dataset.getDataStore()),
                dataset.getDatabaseName(),
                dataset.getDataStore().getDSI().toFullString(),
                dataset.isDeleted()
        );

        if (CollectionUtils.isNotEmpty(dataset.getFields())) {
            for (DatasetField field : dataset.getFields()) {
                dbOperator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type) VALUES(?, ?, ?)", dataset.getGid(), field.getName(), field.getFieldType().getType().toValue());
            }
        }
    }

}
