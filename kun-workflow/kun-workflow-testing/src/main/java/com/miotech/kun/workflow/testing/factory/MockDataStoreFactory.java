package com.miotech.kun.workflow.testing.factory;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.common.URI;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DataStoreType;

import java.util.List;

public class MockDataStoreFactory {
    public static final List<DataStoreType> dataStoreTypes = Lists.newArrayList(
            DataStoreType.ARANGO_COLLECTION,
            DataStoreType.ELASTICSEARCH_INDEX,
            DataStoreType.FILE,
            DataStoreType.HIVE_TABLE,
            DataStoreType.MONGO_COLLECTION,
            DataStoreType.MYSQL_TABLE,
            DataStoreType.POSTGRES_TABLE,
            DataStoreType.SHEET,
            DataStoreType.TOPIC
    );

    public static DataStore getMockDataStore(DataStoreType dataStoreType) {
        return new DataStore(dataStoreType) {
            @Override
            public String getDatabaseName() {
                return this.getType().name();
            }

            @Override
            public URI getURI() {
                return URI.from("randomProtocol://127.0.0.1:8080");
            }
        };
    }
}
