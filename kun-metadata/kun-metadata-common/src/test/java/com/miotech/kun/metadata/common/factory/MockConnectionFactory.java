package com.miotech.kun.metadata.common.factory;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import org.junit.jupiter.params.provider.Arguments;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-16 10:29
 **/
public class MockConnectionFactory {

    public static ConnectionInfo mockConnectionInfo(
            String name,
            Long datasourceId,
            ConnScope connScope,
            ConnectionConfigInfo connectionConfigInfo,
            String description
    ) {
        String user = "test_user";
        return ConnectionInfo.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withDatasourceId(datasourceId)
                .withConnScope(connScope)
                .withName(name)
                .withConnectionConfigInfo(connectionConfigInfo)
                .withDescription(description)
                .withCreateUser(user)
                .withUpdateUser(user)
                .withDeleted(false)
                .build();
    }

    public static ConnectionInfo mockConnectionInfo(
            Long id,
            String name,
            Long datasourceId,
            ConnScope connScope,
            ConnectionConfigInfo connectionConfigInfo,
            String description
    ) {
        String user = "test_user";
        return ConnectionInfo.newBuilder()
                .withId(id)
                .withDatasourceId(datasourceId)
                .withConnScope(connScope)
                .withName(name)
                .withConnectionConfigInfo(connectionConfigInfo)
                .withDescription(description)
                .withCreateUser(user)
                .withUpdateUser(user)
                .withDeleted(false)
                .build();
    }

    public static ConnectionInfo mockConnectionInfo(
            String name,
            ConnScope connScope,
            ConnectionConfigInfo connectionConfigInfo
    ) {
        String user = "test_user";
        return ConnectionInfo.newBuilder()
                .withConnScope(connScope)
                .withName(name)
                .withConnectionConfigInfo(connectionConfigInfo)
                .withCreateUser(user)
                .withUpdateUser(user)
                .withDeleted(false)
                .build();
    }


    public static DatasourceConnection createDatasourceConnection(ConnectionConfigInfo userConfig, ConnectionConfigInfo dataConfig, ConnectionConfigInfo metadataConfig, ConnectionConfigInfo storageConfig) {
        ConnectionInfo user_connection = MockConnectionFactory.mockConnectionInfo("user_connection", ConnScope.USER_CONN, userConfig);
        ConnectionInfo data_connection = MockConnectionFactory.mockConnectionInfo("data_connection", ConnScope.DATA_CONN, dataConfig);
        ConnectionInfo meta_connection = MockConnectionFactory.mockConnectionInfo("meta_connection", ConnScope.METADATA_CONN, metadataConfig);
        ConnectionInfo storage_connection = MockConnectionFactory.mockConnectionInfo("storage_connection", ConnScope.STORAGE_CONN, storageConfig);
        return DatasourceConnection
                .newBuilder()
                .withUserConnectionList(Lists.newArrayList(user_connection))
                .withDataConnection(data_connection)
                .withMetadataConnection(meta_connection)
                .withStorageConnection(storage_connection)
                .build();
    }

    public static DatasourceConnection createDatasourceConnection(ConnectionConfigInfo commConfig) {
        ConnectionInfo user_connection = MockConnectionFactory.mockConnectionInfo("user_connection", ConnScope.USER_CONN, commConfig);
        ConnectionInfo data_connection = MockConnectionFactory.mockConnectionInfo("data_connection", ConnScope.DATA_CONN, commConfig);
        ConnectionInfo meta_connection = MockConnectionFactory.mockConnectionInfo("meta_connection", ConnScope.METADATA_CONN, commConfig);
        ConnectionInfo storage_connection = MockConnectionFactory.mockConnectionInfo("storage_connection", ConnScope.STORAGE_CONN, commConfig);
        return DatasourceConnection
                .newBuilder()
                .withUserConnectionList(Lists.newArrayList(user_connection))
                .withDataConnection(data_connection)
                .withMetadataConnection(meta_connection)
                .withStorageConnection(storage_connection)
                .build();
    }

}
