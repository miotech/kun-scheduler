package com.miotech.kun.metadata.common.factory;

import com.miotech.kun.metadata.core.model.DataSource;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class MockDataSourceFactory {

    private MockDataSourceFactory() {
    }

    public static DataSource createDataSource(long dataSourceId, String name, Map<String, Object> connectionInfo, long typeId, List<String> tags, String createUser, String updateUser) {
        return DataSource.newBuilder()
                .withId(dataSourceId)
                .withTypeId(typeId)
                .withName(name)
                .withConnectionInfo(connectionInfo)
                .withTags(tags)
                .withCreateUser(createUser)
                .withCreateTime(OffsetDateTime.now())
                .withUpdateUser(updateUser)
                .withUpdateTime(OffsetDateTime.now())
                .build();
    }

}
