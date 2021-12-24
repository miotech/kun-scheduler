package com.miotech.kun.metadata.databuilder.factory;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

public class DataSourceFactory {

    private DataSourceFactory() {
    }


    public static DataSource createDataSource(long dataSourceId, String name, ConnectionConfig connectionConfig, DatasourceType type) {
        return DataSource.newBuilder()
                .withId(dataSourceId)
                .withDatasourceType(type)
                .withName(name)
                .withConnectionConfig(connectionConfig)
                .withCreateUser("createUser")
                .withUpdateUser("updateUser")
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .build();
    }
}
