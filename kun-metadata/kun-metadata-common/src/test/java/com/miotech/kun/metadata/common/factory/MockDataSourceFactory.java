package com.miotech.kun.metadata.common.factory;


import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;

import java.time.OffsetDateTime;
import java.util.List;

public class MockDataSourceFactory {

    private MockDataSourceFactory() {
    }

    public static DataSource createDataSource(long dataSourceId, String name, ConnectionInfo connectionInfo, DatasourceType type, List<String> tags) {
        ConnectionConfig connectionConfig = ConnectionConfig.newBuilder().withUserConnection(connectionInfo).build();
        return createDataSource(dataSourceId, name, connectionConfig, type, tags, "admin", "admin");
    }

    public static DataSource createDataSource(long dataSourceId, String name, ConnectionConfig connectionConfig, DatasourceType type, List<String> tags) {
        return createDataSource(dataSourceId, name, connectionConfig, type, tags, "admin", "admin");
    }

    public static DataSource createDataSource(long dataSourceId, String name, ConnectionConfig connectionConfig, DatasourceType type, List<String> tags, String createUser, String updateUser) {
        return DataSource.newBuilder()
                .withId(dataSourceId)
                .withDatasourceType(type)
                .withName(name)
                .withConnectionConfig(connectionConfig)
                .withTags(tags)
                .withCreateUser(createUser)
                .withCreateTime(OffsetDateTime.now())
                .withUpdateUser(updateUser)
                .withUpdateTime(OffsetDateTime.now())
                .build();
    }

    public static DataSourceRequest createRequest(String name, ConnectionInfo connectionInfo, DatasourceType type, List<String> tags) {
        return createRequest(name, ConnectionConfig.newBuilder().withUserConnection(connectionInfo).build(), type, tags);
    }

    public static DataSourceRequest createRequest(String name, ConnectionConfig connectionConfig, DatasourceType type, List<String> tags) {
        return DataSourceRequest.newBuilder()
                .withName(name)
                .withConnectionConfig(connectionConfig)
                .withDatasourceType(type)
                .withTags(tags)
                .withCreateUser("createUser")
                .withUpdateUser("updateUser")
                .build();
    }

}
