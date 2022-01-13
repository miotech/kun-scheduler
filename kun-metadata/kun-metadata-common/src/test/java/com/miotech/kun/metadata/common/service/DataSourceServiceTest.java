package com.miotech.kun.metadata.common.service;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DataSourceServiceTest extends DatabaseTestBase {

    @Inject
    private DataSourceDao dataSourceDao;

    @Inject
    private DataSourceService dataSourceService;

    @Inject
    private DatabaseOperator databaseOperator;

    @Test
    public void createDataSourceHasExist_should_throw_IllegalArgumentException() {
        //prepare
        ConnectionInfo connectionInfo = new PostgresConnectionInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432);
        DataSource saved = MockDataSourceFactory.createDataSource(1, "saved", connectionInfo, DatasourceType.POSTGRESQL, new ArrayList<>());
        dataSourceDao.create(saved);
        DataSourceRequest dataSourceRequest = MockDataSourceFactory.createRequest("new", connectionInfo, DatasourceType.POSTGRESQL, new ArrayList<>());

        //verify
        Exception ex = assertThrows(IllegalArgumentException.class, () -> dataSourceService.create(dataSourceRequest));
        assertEquals("datasource with type " + dataSourceRequest.getDatasourceType()
                + " and connection info " + dataSourceRequest.getConnectionConfig() + " is exist", ex.getMessage());

    }

    @Test
    public void getDataSourceIdByConnectionInfo() {
        //prepare
        ConnectionInfo connectionInfo = new PostgresConnectionInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432);

        DataSource saved = MockDataSourceFactory.createDataSource(1, "saved", connectionInfo, DatasourceType.POSTGRESQL, new ArrayList<>());
        dataSourceDao.create(saved);

        DataStore dataStore = MockDatasetFactory.createDataStore("PostgreSQL", "kun", "test");
        Long dataSourceId = dataSourceService.getDataSourceIdByConnectionInfo(DataStoreType.POSTGRES_TABLE, dataStore.getConnectionInfo());
        DataSource selected = dataSourceDao.findById(dataSourceId).get();

        PostgresConnectionInfo userConnection = (PostgresConnectionInfo) selected.getConnectionConfig().getUserConnection();

        //verify
        assertThat(userConnection.getHost(), is("127.0.0.1"));
        assertThat(userConnection.getPort(), is(5432));
        assertThat(selected.getId(), is(dataSourceId));
        assertThat(selected.getDatasourceType(), is(saved.getDatasourceType()));
    }

    @Test
    public void getDataSourceIdByHiveStore_should_priority_to_return_hive_source() {
        //prepare
        ConnectionInfo hiveServerConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER, "127.0.0.1", 10000);
        ConnectionInfo athenaConnectionInfo = new AthenaConnectionInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        DataSource hive = MockDataSourceFactory.createDataSource(1, "hive", hiveServerConnectionInfo, DatasourceType.HIVE, new ArrayList<>());
        dataSourceDao.create(hive);
        DataSource aws = MockDataSourceFactory.createDataSource(2, "aws", athenaConnectionInfo, DatasourceType.HIVE, new ArrayList<>());
        dataSourceDao.create(aws);

        Long dataSourceId = dataSourceService.getDataSourceIdByConnectionInfo(DataStoreType.HIVE_TABLE
                , hiveServerConnectionInfo);
        DataSource selected = dataSourceDao.findById(dataSourceId).get();

        //verify
        assertThat(selected.getId(), is(dataSourceId));
        assertThat(selected.getDatasourceType(), is(hive.getDatasourceType()));
        assertThat(selected.getName(), is(hive.getName()));
    }

    @Test
    public void getDataSourceIdByHiveStoreHiveSourceNotExist_should_return_aws_source() {
        //prepare
        ConnectionInfo athenaConnectionInfo = new AthenaConnectionInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        DataSource aws = MockDataSourceFactory.createDataSource(1, "aws", athenaConnectionInfo, DatasourceType.HIVE, new ArrayList<>());
        dataSourceDao.create(aws);

        Long dataSourceId = dataSourceService.getDataSourceIdByConnectionInfo(DataStoreType.HIVE_TABLE
                , athenaConnectionInfo);
        DataSource selected = dataSourceDao.findById(dataSourceId).get();

        //verify
        assertThat(selected.getId(), is(dataSourceId));
        assertThat(selected.getDatasourceType(), is(aws.getDatasourceType()));
        assertThat(selected.getName(), is(aws.getName()));
    }

    @Test
    public void createDatasourceWithConnectionConfig() {
        //prepare
        ConnectionInfo userConnectionInfo = new AthenaConnectionInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        ConnectionInfo metadataConnectionInfo = new GlueConnectionInfo(ConnectionType.GLUE, "acesskey", "secretKey", "region");
        ConnectionInfo storageConnectionInfo = new S3ConnectionInfo(ConnectionType.S3, "acesskey", "secretKey", "region");
        ConnectionConfig connectionConfig = ConnectionConfig.newBuilder()
                .withUserConnection(userConnectionInfo)
                .withMetadataConnection(metadataConnectionInfo)
                .withStorageConnection(storageConnectionInfo)
                .build();
        DataSourceRequest aws = MockDataSourceFactory.createRequest("aws", connectionConfig, DatasourceType.HIVE, new ArrayList<>());
        DataSource dataSource = dataSourceService.create(aws);

        DataSource saved = dataSourceDao.findById(dataSource.getId()).get();

        ConnectionConfig savedConnectionConfig = saved.getConnectionConfig();

        //verify
        assertThat(saved.getName(), is(aws.getName()));
        assertThat(saved.getDatasourceType(), is(aws.getDatasourceType()));
        assertThat(savedConnectionConfig.getUserConnection(), is(userConnectionInfo));
        assertThat(savedConnectionConfig.getDataConnection(), is(userConnectionInfo));
        assertThat(savedConnectionConfig.getMetadataConnection(), is(metadataConnectionInfo));
        assertThat(savedConnectionConfig.getStorageConnection(), is(storageConnectionInfo));

    }

}
