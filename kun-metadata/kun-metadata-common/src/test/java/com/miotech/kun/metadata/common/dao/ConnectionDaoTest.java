package com.miotech.kun.metadata.common.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.factory.MockConnectionFactory;
import com.miotech.kun.metadata.core.model.connection.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class ConnectionDaoTest extends DatabaseTestBase {
    @Inject
    ConnectionDao connectionDao;

    public static Stream<Arguments> connectionConfigs() {
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
        Arguments aws = Arguments.arguments(1L, awsConfig);
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
        Arguments hive = Arguments.arguments(2L, hiveConfig);
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
        Arguments pg = Arguments.arguments(3L, pgConfig);

        return Stream.of(aws, hive, pg);
    }

    @ParameterizedTest
    @MethodSource("connectionConfigs")
    void test_add_datasource_connection_pg(Long datasourceId, DatasourceConnection mock_datasourceConnection) {
        List<ConnectionInfo> collect = mock_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.addConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        DatasourceConnection first_insert = new DatasourceConnection(collect);
        assertThat(first_insert.getDatasourceConnectionList().size(), is(first_insert.getDatasourceConnectionList().size()));
        assertThat(first_insert.getUserConnectionList().get(0).getId(), is(Matchers.notNullValue()));
        assertThat(first_insert.getDataConnection().getId(), is(Matchers.notNullValue()));
        assertThat(first_insert.getMetadataConnection().getId(), is(Matchers.notNullValue()));
        assertThat(first_insert.getStorageConnection().getId(), is(Matchers.notNullValue()));
        Optional<DatasourceConnection> fetch_datasourceConnectionOpt = connectionDao.fetchDatasourceConnection(datasourceId);
        assertThat(fetch_datasourceConnectionOpt.isPresent(), is(true));
        DatasourceConnection fetch_datasourceConnection = fetch_datasourceConnectionOpt.get();
        assertThat(fetch_datasourceConnection, is(Matchers.notNullValue()));
        assertThat(fetch_datasourceConnection.getUserConnectionList().size(), is(first_insert.getUserConnectionList().size()));
        assertThat(fetch_datasourceConnection.getDataConnection(), is(first_insert.getDataConnection()));
        assertThat(fetch_datasourceConnection.getMetadataConnection(), is(first_insert.getMetadataConnection()));
        assertThat(fetch_datasourceConnection.getStorageConnection(), is(first_insert.getStorageConnection()));
        assertThat(fetch_datasourceConnection.getUserConnectionList().get(0), is(first_insert.getUserConnectionList().get(0)));
    }

    @Test
    void test_update_datasource_connection_many_user_connection() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        DatasourceConnection mock_datasourceConnection = MockConnectionFactory.createDatasourceConnection(pgConnectionConfigInfo);
        List<ConnectionInfo> collect = mock_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.addConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        DatasourceConnection insert = new DatasourceConnection(collect);
        ConnectionConfigInfo new_user_ConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test2", "test_pw_after");
        ConnectionInfo second_user_connectionInfo = MockConnectionFactory.mockConnectionInfo(null, "second user conn", datasourceId, ConnScope.USER_CONN, new_user_ConnectionConfigInfo, "many use connection");
        List<ConnectionInfo> datasourceConnectionList = insert.getDatasourceConnectionList();
        datasourceConnectionList.add(second_user_connectionInfo);
        DatasourceConnection update_mock_datasourceConnection = new DatasourceConnection(datasourceConnectionList);
        update_mock_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.saveOrUpdateConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        Optional<DatasourceConnection> update_datasourceConnectionOpt = connectionDao.fetchDatasourceConnection(datasourceId);
        assertThat(update_datasourceConnectionOpt.isPresent(), is(true));
        DatasourceConnection update_datasourceConnection = update_datasourceConnectionOpt.get();
        assertThat(update_datasourceConnection.getDatasourceConnectionList().size(), is(5));
        assertThat(update_datasourceConnection.getUserConnectionList().size(), is(2));
        assertThat(update_datasourceConnection.getUserConnectionList().get(0), is(insert.getUserConnectionList().get(0)));
        ConnectionInfo add_connectionInfo = update_datasourceConnection.getUserConnectionList().get(1);
        assertThat(add_connectionInfo.getConnectionConfigInfo(), is(new_user_ConnectionConfigInfo));
        assertThat(add_connectionInfo.getName(), is(second_user_connectionInfo.getName()));
    }

    @Test
    void test_update_datasource_connection_update_config() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        DatasourceConnection mock_datasourceConnection = MockConnectionFactory.createDatasourceConnection(pgConnectionConfigInfo);
        List<ConnectionInfo> collect = mock_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.addConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        DatasourceConnection insert = new DatasourceConnection(collect);
        ConnectionConfigInfo update_ConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test2", "test_pw_after");
        ConnectionInfo connectionInfo = insert.getUserConnectionList().get(0);
        ConnectionInfo update_user_connection = connectionInfo.cloneBuilder().withConnectionConfigInfo(update_ConnectionConfigInfo).build();
        DatasourceConnection mock_update_datasourceConnection = insert.cloneBuilder().withUserConnectionList(Lists.newArrayList(update_user_connection)).build();
        mock_update_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.saveOrUpdateConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        Optional<DatasourceConnection> update_datasourceConnectionOpt = connectionDao.fetchDatasourceConnection(datasourceId);
        assertThat(update_datasourceConnectionOpt.isPresent(), is(true));
        DatasourceConnection update_datasourceConnection = update_datasourceConnectionOpt.get();
        assertThat(update_datasourceConnection.getUserConnectionList().size(), is(1));
        ConnectionInfo update_connectionInfo = update_datasourceConnection.getUserConnectionList().get(0);
        assertThat(update_connectionInfo.getConnectionConfigInfo(), is(update_ConnectionConfigInfo));
        assertThat(update_connectionInfo.getName(), is(update_connectionInfo.getName()));
    }

    @Test
    void test_add_connection() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        ConnectionInfo user_connectionInfo1 = MockConnectionFactory.mockConnectionInfo(null, "user conn", datasourceId, ConnScope.USER_CONN, pgConnectionConfigInfo, "connection");
        ConnectionInfo connectionInfo1 = connectionDao.addConnection(datasourceId, user_connectionInfo1);
        Optional<ConnectionInfo> connection = connectionDao.findConnection(connectionInfo1.getId());
        assertThat(connection.isPresent(), is(true));
        assertThat(connection.orElse(null), is(connectionInfo1));
    }

    @Test
    void test_saveOrUpdate_Connection() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo_1 = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        ConnectionInfo mock_info1 = MockConnectionFactory.mockConnectionInfo(null, "user conn 1", datasourceId, ConnScope.USER_CONN, pgConnectionConfigInfo_1, "connection");
        ConnectionInfo add_info_1 = connectionDao.addConnection(datasourceId, mock_info1);

        ConnectionConfigInfo pgConnectionConfigInfo_2 = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test2", "test_pw2");
        ConnectionInfo mock_info2 = MockConnectionFactory.mockConnectionInfo(null, "user conn 2", datasourceId, ConnScope.USER_CONN, pgConnectionConfigInfo_2, "connection");
        ConnectionInfo add_info_2 = connectionDao.addConnection(datasourceId, mock_info2);
//        update info1 config to info2
        ConnectionInfo sameConfig = add_info_1.cloneBuilder().withConnectionConfigInfo(pgConnectionConfigInfo_2).build();
        Exception ex1 = assertThrows(IllegalArgumentException.class, () -> connectionDao.saveOrUpdateConnection(datasourceId, sameConfig));
        assertEquals(String.format("connection config exist,same conn name:%s", add_info_2.getName()), ex1.getMessage());
//        update info1 name to info2
        ConnectionInfo sameName = add_info_1.cloneBuilder().withName(add_info_2.getName()).build();
        Exception ex2 = assertThrows(IllegalArgumentException.class, () -> connectionDao.saveOrUpdateConnection(datasourceId, sameName));
        assertEquals(String.format("connection name exist,same conn name:%s", add_info_2.getName()), ex2.getMessage());


    }

    @Test
    void test_find_connection() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        ConnectionInfo user_connectionInfo1 = MockConnectionFactory.mockConnectionInfo(null, "user conn", datasourceId, ConnScope.USER_CONN, pgConnectionConfigInfo, "connection");
        ConnectionInfo connectionInfo1 = connectionDao.addConnection(datasourceId, user_connectionInfo1);
        Optional<ConnectionInfo> connection = connectionDao.findConnection(connectionInfo1.getId());
        assertThat(connection.isPresent(), is(true));
        assertThat(connection, is(Optional.of(connectionInfo1)));
        Optional<ConnectionInfo> connection1 = connectionDao.findConnection(0L);
        assertThat(connection1.isPresent(), is(false));
    }

    @ParameterizedTest
    @MethodSource("connectionConfigs")
    void test_fetch_datasource_connection(Long datasourceId, DatasourceConnection mock_datasourceConnection) {
        List<ConnectionInfo> collect = mock_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.addConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        DatasourceConnection insert = new DatasourceConnection(collect);
        Optional<DatasourceConnection> fetch_datasourceConnectionOpt = connectionDao.fetchDatasourceConnection(datasourceId);
        assertThat(fetch_datasourceConnectionOpt.isPresent(), is(true));
        DatasourceConnection fetch_datasourceConnection = fetch_datasourceConnectionOpt.get();
        assertThat(fetch_datasourceConnection, is(Matchers.notNullValue()));
        assertThat(fetch_datasourceConnection.getUserConnectionList().size(), is(insert.getUserConnectionList().size()));
        assertThat(fetch_datasourceConnection.getDataConnection(), is(insert.getDataConnection()));
        assertThat(fetch_datasourceConnection.getMetadataConnection(), is(insert.getMetadataConnection()));
        assertThat(fetch_datasourceConnection.getStorageConnection(), is(insert.getStorageConnection()));
        assertThat(fetch_datasourceConnection.getUserConnectionList().get(0), is(insert.getUserConnectionList().get(0)));
        Optional<DatasourceConnection> datasourceConnection = connectionDao.fetchDatasourceConnection(System.currentTimeMillis());
        assertThat(datasourceConnection.isPresent(), is(false));
    }

    @ParameterizedTest
    @MethodSource("connectionConfigs")
    void test_fetch_datasource_connection_list(Long datasourceId, DatasourceConnection mock_datasourceConnection) {
        List<ConnectionInfo> collect = mock_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.addConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        DatasourceConnection insert = new DatasourceConnection(collect);
        Optional<DatasourceConnection> fetch_datasourceConnectionOpt = connectionDao.fetchDatasourceConnection(datasourceId);
        assertThat(fetch_datasourceConnectionOpt.isPresent(), is(true));
        DatasourceConnection fetch_datasourceConnection = fetch_datasourceConnectionOpt.get();
        List<ConnectionInfo> user_connectionInfos = connectionDao.fetchDatasourceConnectionList(datasourceId, ConnScope.USER_CONN);
        assertThat(user_connectionInfos.size(), is(1));
        assertThat(user_connectionInfos.get(0), is(fetch_datasourceConnection.getUserConnectionList().get(0)));
        List<ConnectionInfo> data_connectionInfos = connectionDao.fetchDatasourceConnectionList(datasourceId, ConnScope.DATA_CONN);
        assertThat(data_connectionInfos.size(), is(1));
        assertThat(data_connectionInfos.get(0), is(fetch_datasourceConnection.getDataConnection()));
        List<ConnectionInfo> metadata_connectionInfos = connectionDao.fetchDatasourceConnectionList(datasourceId, ConnScope.METADATA_CONN);
        assertThat(metadata_connectionInfos.size(), is(1));
        assertThat(metadata_connectionInfos.get(0), is(fetch_datasourceConnection.getMetadataConnection()));
        List<ConnectionInfo> storage_connectionInfos = connectionDao.fetchDatasourceConnectionList(datasourceId, ConnScope.STORAGE_CONN);
        assertThat(storage_connectionInfos.size(), is(1));
        assertThat(storage_connectionInfos.get(0), is(fetch_datasourceConnection.getStorageConnection()));
    }

    @Test
    void test_fetch_connScope_connection() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        ConnectionInfo user_connectionInfo1 = MockConnectionFactory.mockConnectionInfo(null, "user conn", datasourceId, ConnScope.USER_CONN, pgConnectionConfigInfo, "connection");
        ConnectionInfo user_connectionInfo = connectionDao.addConnection(datasourceId, user_connectionInfo1);
        List<ConnectionInfo> connectionInfos = connectionDao.fetchConnScopeConnection(ConnScope.USER_CONN);
        assertThat(CollectionUtils.isNotEmpty(connectionInfos), is(true));
        assertThat(connectionInfos.get(0), is(user_connectionInfo));
        List<ConnectionInfo> data_connectionInfos = connectionDao.fetchConnScopeConnection(ConnScope.DATA_CONN);
        assertThat(CollectionUtils.isNotEmpty(data_connectionInfos), is(false));
    }

    @Test
    void test_remove_connection() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        ConnectionInfo user_connectionInfo1 = MockConnectionFactory.mockConnectionInfo(null, "user conn", datasourceId, ConnScope.USER_CONN, pgConnectionConfigInfo, "connection");
        ConnectionInfo connectionInfo1 = connectionDao.addConnection(datasourceId, user_connectionInfo1);
        Optional<ConnectionInfo> connection = connectionDao.findConnection(connectionInfo1.getId());
        assertThat(connection.isPresent(), is(true));
        assertThat(connection, is(Optional.of(connectionInfo1)));
        ConnectionInfo remove_connectionInfo = connectionDao.removeConnection(connection.get());
        assertThat(remove_connectionInfo, Matchers.not(connection));
        assertThat(remove_connectionInfo.getDeleted(), is(true));
        Optional<ConnectionInfo> remove_connection = connectionDao.findConnection(connection.get().getId());
        assertThat(remove_connection.isPresent(), is(false));
    }

    @Test
    void test_remove_connection_id() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        ConnectionInfo user_connectionInfo1 = MockConnectionFactory.mockConnectionInfo(null, "user conn", datasourceId, ConnScope.USER_CONN, pgConnectionConfigInfo, "connection");
        ConnectionInfo connectionInfo1 = connectionDao.addConnection(datasourceId, user_connectionInfo1);
        Optional<ConnectionInfo> connection = connectionDao.findConnection(connectionInfo1.getId());
        assertThat(connection.isPresent(), is(true));
        assertThat(connection, is(Optional.of(connectionInfo1)));
        connectionDao.removeConnection(connection.get().getId());
        Optional<ConnectionInfo> remove_connection = connectionDao.findConnection(connection.get().getId());
        assertThat(remove_connection.isPresent(), is(false));
    }

    @ParameterizedTest
    @MethodSource("connectionConfigs")
    void test_remove_connection_by_datasource(Long datasourceId, DatasourceConnection mock_datasourceConnection) {
        List<ConnectionInfo> collect = mock_datasourceConnection.getDatasourceConnectionList().stream().map(mock_conn -> connectionDao.addConnection(datasourceId, mock_conn)).collect(Collectors.toList());
        DatasourceConnection first_insert = new DatasourceConnection(collect);
        assertThat(first_insert.getDatasourceConnectionList().size(), is(first_insert.getDatasourceConnectionList().size()));
        assertThat(first_insert.getUserConnectionList().get(0).getId(), is(Matchers.notNullValue()));
        assertThat(first_insert.getDataConnection().getId(), is(Matchers.notNullValue()));
        assertThat(first_insert.getMetadataConnection().getId(), is(Matchers.notNullValue()));
        assertThat(first_insert.getStorageConnection().getId(), is(Matchers.notNullValue()));
        connectionDao.fetchDatasourceConnection(datasourceId);
        connectionDao.removeConnectionByDatasource(datasourceId);
        Optional<DatasourceConnection> datasourceConnectionOpt = connectionDao.fetchDatasourceConnection(datasourceId);
        assertThat(datasourceConnectionOpt.isPresent(), is(false));
    }
}