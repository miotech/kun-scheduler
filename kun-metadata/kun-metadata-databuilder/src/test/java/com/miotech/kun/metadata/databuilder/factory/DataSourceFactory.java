package com.miotech.kun.metadata.databuilder.factory;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

public class DataSourceFactory {

    private DataSourceFactory() {
    }

    public static DataSource create() {
        return create(1L, new ConnectionInfo(ImmutableMap.of()));
    }

    public static DataSource create(Long typeId, ConnectionInfo connectionInfo) {
        return DataSource.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName("Test-DataSource")
                .withConnectionInfo(connectionInfo)
                .withTypeId(typeId)
                .withTags(ImmutableList.of("a", "b", "c"))
                .withCreateUser("admin")
                .withCreateTime(DateTimeUtils.now())
                .withUpdateUser("admin")
                .withUpdateTime(DateTimeUtils.now())
                .build();
    }
}
