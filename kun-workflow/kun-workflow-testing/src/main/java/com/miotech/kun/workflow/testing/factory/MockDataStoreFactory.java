package com.miotech.kun.workflow.testing.factory;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static DataStore getMockDataStore(DataStoreType dataStoreType, String database, String table) {
        return new DataStore(dataStoreType) {
            @Override
            public String getDatabaseName() {
                return this.getType().name();
            }

            @Override
            public String getLocationInfo() {
                if (getType().equals(DataStoreType.HIVE_TABLE)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(database).append(":").append(table);
                    return stringBuilder.toString();
                }
                return "";
            }

            @Nullable
            @Override
            public ConnectionConfigInfo getConnectionConfigInfo() {
                return null;
            }


            @Override
            public String getName() {
                return table;
            }
        };
    }
}
