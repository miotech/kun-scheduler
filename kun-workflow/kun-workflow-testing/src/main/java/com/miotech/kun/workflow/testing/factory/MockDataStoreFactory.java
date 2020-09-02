package com.miotech.kun.workflow.testing.factory;

import com.google.common.collect.Lists;
import com.miotech.kun.workflow.core.model.common.URI;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.DataStoreType;

import java.util.List;
import java.util.Random;

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

    public static DataStoreType getRandomDataStoreType() {
        Random random = new Random();
        return dataStoreTypes.get(random.nextInt(dataStoreTypes.size()));
    }

    public static DataStore getMockDataStore() {
        return new DataStore(getRandomDataStoreType()) {
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
