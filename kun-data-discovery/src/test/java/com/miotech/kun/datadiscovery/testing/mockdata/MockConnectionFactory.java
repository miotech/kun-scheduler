package com.miotech.kun.datadiscovery.testing.mockdata;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.entity.SecurityInfo;
import com.miotech.kun.datadiscovery.model.enums.ConnectionRole;
import com.miotech.kun.datadiscovery.model.enums.SecurityModule;
import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoSecurityVO;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import org.junit.jupiter.params.provider.Arguments;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;
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

    public static DatasourceConnection createDatasourceConnection(Long datasourceId, ConnectionConfigInfo commConfig) {
        ConnectionInfo user_connection = MockConnectionFactory.mockConnectionInfo(1L, "user_connection", datasourceId, ConnScope.USER_CONN, commConfig, "user_connection");
        ConnectionInfo data_connection = MockConnectionFactory.mockConnectionInfo(2L, "data_connection", datasourceId, ConnScope.DATA_CONN, commConfig, "data_connection");
        ConnectionInfo meta_connection = MockConnectionFactory.mockConnectionInfo(3L, "meta_connection", datasourceId, ConnScope.METADATA_CONN, commConfig, "meta_connection");
        ConnectionInfo storage_connection = MockConnectionFactory.mockConnectionInfo(4L, "storage_connection", datasourceId, ConnScope.STORAGE_CONN, commConfig, "storage_connection");
        return DatasourceConnection
                .newBuilder()
                .withUserConnectionList(Lists.newArrayList(user_connection))
                .withDataConnection(data_connection)
                .withMetadataConnection(meta_connection)
                .withStorageConnection(storage_connection)
                .build();
    }

    public static ConnectionInfoSecurityVO mockConnectionInfoSecurityVO(ConnScope connScope) {
        ConnectionInfoSecurityVO connectionInfoSecurityVO = new ConnectionInfoSecurityVO();
        connectionInfoSecurityVO.setId(1L);
        ConnectionConfigInfo userConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        connectionInfoSecurityVO.setConnectionConfigInfo(userConnectionConfigInfo);
        connectionInfoSecurityVO.setConnScope(connScope);
        connectionInfoSecurityVO.setDatasourceId(1L);
        connectionInfoSecurityVO.setDeleted(false);
        connectionInfoSecurityVO.setCreateUser("user");
        connectionInfoSecurityVO.setDescription("desc");
        connectionInfoSecurityVO.setUpdateUser("user");
        connectionInfoSecurityVO.setCreateTime(OffsetDateTime.now());
        connectionInfoSecurityVO.setCreateTime(OffsetDateTime.now());
        connectionInfoSecurityVO.setSecurityUserList(ImmutableList.of("user1", "user2"));
        connectionInfoSecurityVO.setSecurityInfo(new SecurityInfo(SecurityModule.CONNECTION, 1L, ConnectionRole.CONNECTION_USER, ConnectionRole.CONNECTION_USER.getUserOperation()));
        return connectionInfoSecurityVO;
    }


    public static Stream<Arguments> connectionConfigs() {
        Map<String, Object> awsDatasourceConfigInfo = MockDataSourceFactory.createAthenaDatasourceConfig("jdbc:awsathena");
        ConnectionConfigInfo userConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        ConnectionInfo user_conn = MockConnectionFactory.mockConnectionInfo("user_conn", ConnScope.USER_CONN, userConnectionConfigInfo);
        ConnectionInfo data_conn = MockConnectionFactory.mockConnectionInfo("data_conn", ConnScope.DATA_CONN, userConnectionConfigInfo);
        ConnectionConfigInfo metadataConnectionConfigInfo = new GlueConnectionConfigInfo(ConnectionType.GLUE, "glue", "glue", "glue");
        ConnectionInfo meta_conn = MockConnectionFactory.mockConnectionInfo("meta_conn", ConnScope.METADATA_CONN, metadataConnectionConfigInfo);
        ConnectionConfigInfo storageConnectionConfigInfo = new S3ConnectionConfigInfo(ConnectionType.S3, "glue", "glue", "glue");
        ConnectionInfo storage_conn = MockConnectionFactory.mockConnectionInfo("storage_conn", ConnScope.STORAGE_CONN, storageConnectionConfigInfo);

        DatasourceConnection awsConfig = DatasourceConnection.newBuilder()
                .withUserConnectionList(ImmutableList.of(user_conn))
                .withDataConnection(data_conn)
                .withMetadataConnection(meta_conn)
                .withStorageConnection(storage_conn)
                .build();
        Arguments aws = Arguments.arguments(DatasourceType.HIVE, awsDatasourceConfigInfo, awsConfig);
        Map<String, Object> hiveDatasourceConfigInfo = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        ConnectionConfigInfo hiveConnectionConfigInfo = new HiveServerConnectionConfigInfo(ConnectionType.HIVE_SERVER, "host", 10000, "user", "password");
        ConnectionInfo hive_user_conn = MockConnectionFactory.mockConnectionInfo("user_conn", ConnScope.USER_CONN, hiveConnectionConfigInfo);
        ConnectionInfo hive_data_conn = MockConnectionFactory.mockConnectionInfo("data_conn", ConnScope.DATA_CONN, hiveConnectionConfigInfo);
        ConnectionConfigInfo metastoreConnectionConfigInfo = new HiveMetaStoreConnectionConfigInfo(ConnectionType.HIVE_THRIFT, "url");
        ConnectionInfo hive_meta_conn = MockConnectionFactory.mockConnectionInfo("meta_conn", ConnScope.METADATA_CONN, metastoreConnectionConfigInfo);
        ConnectionInfo hive_storage_conn = MockConnectionFactory.mockConnectionInfo("storage_conn", ConnScope.STORAGE_CONN, metastoreConnectionConfigInfo);

        DatasourceConnection hiveConfig = DatasourceConnection.newBuilder()
                .withUserConnectionList(ImmutableList.of(hive_user_conn))
                .withDataConnection(hive_data_conn)
                .withMetadataConnection(hive_meta_conn)
                .withStorageConnection(hive_storage_conn)
                .build();
        Arguments hive = Arguments.arguments(DatasourceType.HIVE, hiveDatasourceConfigInfo, hiveConfig);
        Map<String, Object> pgDatasourceConfigInfo = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5432);
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432);
        ConnectionInfo pg_user_conn = MockConnectionFactory.mockConnectionInfo("user_conn", ConnScope.USER_CONN, pgConnectionConfigInfo);
        ConnectionInfo pg_data_conn = MockConnectionFactory.mockConnectionInfo("data_conn", ConnScope.DATA_CONN, pgConnectionConfigInfo);
        ConnectionInfo pg_metadata_conn = MockConnectionFactory.mockConnectionInfo("metadata_conn", ConnScope.METADATA_CONN, pgConnectionConfigInfo);
        ConnectionInfo pg_storage_conn = MockConnectionFactory.mockConnectionInfo("storage_conn", ConnScope.STORAGE_CONN, pgConnectionConfigInfo);

        DatasourceConnection pgConfig = DatasourceConnection.newBuilder()
                .withUserConnectionList(ImmutableList.of(pg_user_conn))
                .withDataConnection(pg_data_conn)
                .withMetadataConnection(pg_metadata_conn)
                .withStorageConnection(pg_storage_conn)
                .build();
        Arguments pg = Arguments.arguments(DatasourceType.POSTGRESQL, pgDatasourceConfigInfo, pgConfig);

        return Stream.of(aws, hive, pg);
    }


}
