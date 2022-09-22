package com.miotech.kun.metadata.common.cataloger;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.client.ClientFactory;
import com.miotech.kun.metadata.common.factory.MockConnectionFactory;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.service.ConnectionService;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


public class CatalogerTest extends DatabaseTestBase {

    @Inject
    private FieldMappingService fieldMappingService;
    @Inject
    private DataSourceService dataSourceService;
    @Inject
    private ConnectionService connectionService;


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

    @ParameterizedTest
    @MethodSource("connectionConfigs")
    public void testGenerateCataloger(DatasourceType datasourceType, Map<String, Object> datasourceConfigInfo, DatasourceConnection datasourceConnection) {
        ClientFactory clientFactory = mock(ClientFactory.class);
        CatalogerFactory catalogerFactory = new CatalogerFactory(fieldMappingService, clientFactory);
        DataSourceRequest dataSourceRequest = MockDataSourceFactory.createRequest("database_name", datasourceConfigInfo, datasourceConnection, datasourceType, new ArrayList<>(), "admin");
        DataSource dataSource = dataSourceService.create(dataSourceRequest);
        Cataloger cataloger = catalogerFactory.generateCataloger(dataSource);
        MatcherAssert.assertThat(cataloger, notNullValue());
    }
}
