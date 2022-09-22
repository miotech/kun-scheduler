package com.miotech.kun.metadata.common.service;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.factory.MockConnectionFactory;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.utils.MetadataTypeConvertor;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

public class DataSourceServiceTest extends DatabaseTestBase {


    @Inject
    private DataSourceService dataSourceService;

    @Inject
    private DatabaseOperator databaseOperator;

    @Test
    public void createDataSourceHasExist() {
        //prepare
        String host = "127.0.0.1";
        Integer port = 5432;
        PostgresConnectionConfigInfo postgresConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "user", "password");
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(postgresConnectionConfigInfo);
        Map<String, Object> datasourceConfigInfo = MockDataSourceFactory.createHostPortDatasourceConfig(host, port);
        DataSourceRequest saved = MockDataSourceFactory.createRequest("saved", datasourceConfigInfo, datasourceConnection, DatasourceType.POSTGRESQL, new ArrayList<>(), "admin");
        dataSourceService.create(saved);
        //verify
        Exception ex = assertThrows(IllegalArgumentException.class, () -> dataSourceService.create(saved));
        Assertions.assertEquals("datasource with type " + saved.getDatasourceType()
                + " and name " + saved.getName() + " is exist", ex.getMessage());
        DataSourceRequest newSaved = MockDataSourceFactory.createRequest("new datasource", datasourceConfigInfo, datasourceConnection, DatasourceType.POSTGRESQL, new ArrayList<>(), "admin");
        Exception ex1 = assertThrows(IllegalArgumentException.class, () -> dataSourceService.create(newSaved));
        Assertions.assertEquals("datasource with type " + saved.getDatasourceType()
                + " and connection info " + saved.getDatasourceConfigInfo() + " is exist", ex1.getMessage());

    }

    @Test
    public void getDataSourceIdByConnectionInfo() {
        //prepare
        String host = "127.0.0.1";
        Integer port = 5432;
        PostgresConnectionConfigInfo postgresConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "user", "password");
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(postgresConnectionConfigInfo);
        Map<String, Object> datasourceConfigInfo = MockDataSourceFactory.createHostPortDatasourceConfig(host, port);
        DataSourceRequest saved = MockDataSourceFactory.createRequest("saved", datasourceConfigInfo, datasourceConnection, DatasourceType.POSTGRESQL, new ArrayList<>(), "admin");
        dataSourceService.create(saved);

        DataStore dataStore = MockDatasetFactory.createDataStore(DataStoreType.POSTGRES_TABLE, "kun", "test");
        DatasourceType datasourceType = MetadataTypeConvertor.covertStoreTypeToSourceType(dataStore.getType());
        Long dataSourceId = dataSourceService.fetchDataSourceByConnectionInfo(datasourceType, datasourceConnection.getDataConnection().getConnectionConfigInfo());
        Optional<DataSource> selected = dataSourceService.fetchDatasource(dataSourceId);
        Assertions.assertTrue(selected.isPresent());
        DataSource dataSourceSelected = selected.get();
        PostgresConnectionConfigInfo userConnection = (PostgresConnectionConfigInfo) dataSourceSelected.getDatasourceConnection().getUserConnectionList().get(0).getConnectionConfigInfo();
        //verify
        assertThat(userConnection.getHost(), is(host));
        assertThat(userConnection.getPort(), is(port));
        assertThat(dataSourceSelected.getId(), is(dataSourceId));
        assertThat(dataSourceSelected.getDatasourceType(), is(saved.getDatasourceType()));
    }

    @Test
    public void getDataSourceIdByHiveStore_should_priority_to_return_hive_source() {
        //prepare
        ConnectionConfigInfo hiveServerConnectionConfigInfo = new HiveServerConnectionConfigInfo(ConnectionType.HIVE_SERVER, "127.0.0.1", 10000);
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        DatasourceConnection hiveDatasourceConnection = MockConnectionFactory.createDatasourceConnection(hiveServerConnectionConfigInfo);
        DataSourceRequest hive = MockDataSourceFactory.createRequest("hive", hostPortDatasourceConfig, hiveDatasourceConnection, DatasourceType.HIVE, new ArrayList<>(), "admin");
        dataSourceService.create(hive);
        Map<String, Object> athenaDatasourceConfig = MockDataSourceFactory.createAthenaDatasourceConfig("jdbc:awsathena");
        ConnectionConfigInfo athenaConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        DatasourceConnection awsDatasourceConnection = MockConnectionFactory.createDatasourceConnection(athenaConnectionConfigInfo);
        DataSourceRequest aws = MockDataSourceFactory.createRequest("aws", athenaDatasourceConfig, awsDatasourceConnection, DatasourceType.HIVE, new ArrayList<>(), "admin");
        dataSourceService.create(aws);
        DatasourceType datasourceType = MetadataTypeConvertor.covertStoreTypeToSourceType(DataStoreType.HIVE_TABLE);
        Long dataSourceId = dataSourceService.fetchDataSourceByConnectionInfo(datasourceType, hiveServerConnectionConfigInfo);
        Optional<DataSource> selected = dataSourceService.fetchDatasource(dataSourceId);
        Assertions.assertTrue(selected.isPresent());
        DataSource dataSourceSelected = selected.get();

        //verify
        assertThat(dataSourceSelected.getId(), is(dataSourceId));
        assertThat(dataSourceSelected.getDatasourceType(), is(hive.getDatasourceType()));
        assertThat(dataSourceSelected.getName(), is(hive.getName()));
    }

    @Test
    public void getDataSourceIdByHiveStoreHiveSourceNotExist_should_return_aws_source() {
        //prepare
        Map<String, Object> athenaDatasourceConfig = MockDataSourceFactory.createAthenaDatasourceConfig("jdbc:awsathena");
        ConnectionConfigInfo athenaConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        DatasourceConnection awsDatasourceConnection = MockConnectionFactory.createDatasourceConnection(athenaConnectionConfigInfo);
        DataSourceRequest aws = MockDataSourceFactory.createRequest("aws", athenaDatasourceConfig, awsDatasourceConnection, DatasourceType.HIVE, new ArrayList<>(), "admin");
        dataSourceService.create(aws);
        DatasourceType datasourceType = MetadataTypeConvertor.covertStoreTypeToSourceType(DataStoreType.HIVE_TABLE);
        Long dataSourceId = dataSourceService.fetchDataSourceByConnectionInfo(datasourceType, athenaConnectionConfigInfo);
        Optional<DataSource> selected = dataSourceService.fetchDatasource(dataSourceId);
        Assertions.assertTrue(selected.isPresent());
        DataSource dataSourceSelected = selected.get();
        //verify
        assertThat(dataSourceSelected.getId(), is(dataSourceId));
        assertThat(dataSourceSelected.getDatasourceType(), is(aws.getDatasourceType()));
        assertThat(dataSourceSelected.getName(), is(aws.getName()));
    }

    @Test
    public void createDatasourceWithConnectionConfig() {
        //prepare
        ConnectionConfigInfo userConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        ConnectionConfigInfo metadataConnectionConfigInfo = new GlueConnectionConfigInfo(ConnectionType.GLUE, "acesskey", "secretKey", "region");
        ConnectionConfigInfo storageConnectionConfigInfo = new S3ConnectionConfigInfo(ConnectionType.S3, "acesskey", "secretKey", "region");
        Map<String, Object> athenaDatasourceConfig = MockDataSourceFactory.createAthenaDatasourceConfig("jdbc:awsathena");
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(userConnectionConfigInfo, userConnectionConfigInfo, metadataConnectionConfigInfo, storageConnectionConfigInfo);
        DataSourceRequest aws = MockDataSourceFactory.createRequest("aws", athenaDatasourceConfig, datasourceConnection, DatasourceType.HIVE, new ArrayList<>(), "admin");
        DataSource dataSource = dataSourceService.create(aws);

        Optional<DataSource> selected = dataSourceService.fetchDatasource(dataSource.getId());
        Assertions.assertTrue(selected.isPresent());
        DataSource dataSourceSelected = selected.get();
        DatasourceConnection savedDatasourceConnection = dataSourceSelected.getDatasourceConnection();

        //verify
        assertThat(dataSourceSelected.getName(), is(aws.getName()));
        assertThat(dataSourceSelected.getDatasourceType(), is(aws.getDatasourceType()));
        assertThat(savedDatasourceConnection.getUserConnectionList().get(0).getConnectionConfigInfo(), is(userConnectionConfigInfo));
        assertThat(savedDatasourceConnection.getDataConnection().getConnectionConfigInfo(), is(userConnectionConfigInfo));
        assertThat(savedDatasourceConnection.getMetadataConnection().getConnectionConfigInfo(), is(metadataConnectionConfigInfo));
        assertThat(savedDatasourceConnection.getStorageConnection().getConnectionConfigInfo(), is(storageConnectionConfigInfo));

    }

}
