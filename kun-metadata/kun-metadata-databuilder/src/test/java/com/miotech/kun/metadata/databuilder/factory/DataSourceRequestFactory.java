package com.miotech.kun.metadata.databuilder.factory;

import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

public class DataSourceRequestFactory {

    private DataSourceRequestFactory() {
    }

    public static DataSourceRequest create(Long typeId, ConnectionInfo connectionInfo) {
        return DataSourceRequest.newBuilder()
                .withName("Test-DataSource")
                .withTypeId(typeId)
                .withConnectionInfo(connectionInfo)
                .withTags(ImmutableList.of())
                .withCreateUser("admin")
                .withUpdateUser("admin")
                .build();
    }

    public static DataSourceRequest create() {
        return create(1L, new ConnectionInfo(ImmutableMap.of()));
    }
}
