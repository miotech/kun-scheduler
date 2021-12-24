package com.miotech.kun.metadata.databuilder.factory;

import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

public class DataSourceRequestFactory {

    private DataSourceRequestFactory() {
    }

    public static DataSourceRequest create(DatasourceType type, ConnectionConfig connectionConfig) {
        return DataSourceRequest.newBuilder()
                .withName("Test-DataSource")
                .withDatasourceType(type)
                .withConnectionConfig(connectionConfig)
                .withTags(ImmutableList.of())
                .withCreateUser("admin")
                .withUpdateUser("admin")
                .build();
    }

}
