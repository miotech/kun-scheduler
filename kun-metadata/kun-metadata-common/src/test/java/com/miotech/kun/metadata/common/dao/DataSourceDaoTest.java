package com.miotech.kun.metadata.common.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.HiveServerConnectionInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceSearchFilter;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataSourceDaoTest extends DatabaseTestBase {

    @Inject
    private DataSourceDao dataSourceDao;

    @Test
    public void testFetchById_withExistGid() {
        // Prepare
        ConnectionInfo hiveServerConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"127.0.0.1",10000);
        DataSource dataSource = MockDataSourceFactory.createDataSource(1L, "Hive", hiveServerConnectionInfo, DatasourceType.HIVE, Lists.newArrayList("test"));
        dataSourceDao.create(dataSource);

        // Execute
        Optional<DataSource> existDataSourceOpt = dataSourceDao.findById(1L);
        Optional<DataSource> nonExistDataSourceOpt = dataSourceDao.findById(2L);

        // Validate
        assertTrue(existDataSourceOpt.isPresent());
        DataSource existDataSource = existDataSourceOpt.get();
        assertThat(existDataSource.getId(), is(1L));
        assertThat(existDataSource.getName(), is("Hive"));
        assertThat(existDataSource.getTags(), containsInAnyOrder("test"));
        assertThat(existDataSource.getDatasourceType(), is(DatasourceType.HIVE));

        assertFalse(nonExistDataSourceOpt.isPresent());
    }

    @Test
    public void testFetchTotalCountWithFilter() {
        // Prepare
        ConnectionInfo hiveServerConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"127.0.0.1",10000);
        DataSource dataSource = MockDataSourceFactory.createDataSource(1L, "Hive", hiveServerConnectionInfo, DatasourceType.HIVE, Lists.newArrayList("test"));
        dataSourceDao.create(dataSource);

        // Execute
        Integer shouldExistCount = dataSourceDao.fetchTotalCountWithFilter(new DataSourceSearchFilter("Hive", 1, 20));
        Integer nonExistCount = dataSourceDao.fetchTotalCountWithFilter(new DataSourceSearchFilter("HBase", 1, 20));

        // Validate
        assertThat(shouldExistCount, is(1));
        assertThat(nonExistCount, is(0));
    }

    @Test
    public void testFetchWithFilter() {
        // Prepare
        ConnectionInfo hiveServerConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"127.0.0.1",10000);
        DataSource dataSource = MockDataSourceFactory.createDataSource(1L, "Hive", hiveServerConnectionInfo, DatasourceType.HIVE, Lists.newArrayList("test"));
        dataSourceDao.create(dataSource);

        // Execute
        List<DataSource> shouldExistDataSources = dataSourceDao.fetchWithFilter(new DataSourceSearchFilter("Hive", 1, 20));
        List<DataSource> nonExistDataSources = dataSourceDao.fetchWithFilter(new DataSourceSearchFilter("HBase", 1, 20));

        // Validate
        assertThat(shouldExistDataSources.size(), is(1));
        DataSource dataSourceOfFetch = shouldExistDataSources.get(0);
        assertThat(dataSourceOfFetch.getId(), is(1L));
        assertThat(dataSourceOfFetch.getName(), is("Hive"));
        assertThat(dataSourceOfFetch.getTags(), containsInAnyOrder("test"));
        assertThat(dataSourceOfFetch.getDatasourceType(), is(DatasourceType.HIVE));

        assertThat(nonExistDataSources.size(), is(0));
    }

    @Test
    public void testCreate() {
        // Prepare
        ConnectionInfo hiveServerConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"127.0.0.1",10000);
        DataSource dataSource = MockDataSourceFactory.createDataSource(1L, "Hive", hiveServerConnectionInfo, DatasourceType.HIVE, Lists.newArrayList("test"));

        // Execute
        dataSourceDao.create(dataSource);
        // Validate
        Optional<DataSource> dataSourceOpt = dataSourceDao.findById(1L);


        assertTrue(dataSourceOpt.isPresent());
        DataSource dataSourceOfFetch = dataSourceOpt.get();
        assertThat(dataSourceOfFetch.getId(), is(1L));
        assertThat(dataSourceOfFetch.getName(), is("Hive"));

        HiveServerConnectionInfo connectionInfo = (HiveServerConnectionInfo) dataSourceOpt.get().getConnectionConfig().getUserConnection();
        assertThat(connectionInfo.getHost(), is("127.0.0.1"));
        assertThat(connectionInfo.getPort(), is(10000));
        assertThat(dataSourceOfFetch.getTags(), containsInAnyOrder("test"));
        assertThat(dataSourceOfFetch.getDatasourceType(), is(DatasourceType.HIVE));
    }

    @Test
    public void testUpdate() {
        // Prepare
        ConnectionInfo hiveServerConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"127.0.0.1",10000);
        OffsetDateTime now = DateTimeUtils.now();
        DataSource dataSource = MockDataSourceFactory.createDataSource(1L, "Hive",hiveServerConnectionInfo,DatasourceType.HIVE, Lists.newArrayList("test"));
        dataSourceDao.create(dataSource);

        // Execute
        OffsetDateTime updateTime = now.minusMinutes(1);
        ConnectionConfig connectionConfig = ConnectionConfig
                .newBuilder()
                .withUserConnection(new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"192.168.1.101",10001))
                .build();
        DataSource dataSourceForUpdate = dataSource.cloneBuilder()
                .withConnectionConfig(connectionConfig)
                .withUpdateUser("updater")
                .withUpdateTime(updateTime)
                .withTags(Lists.newArrayList("tag1", "tag2"))
                .build();
        dataSourceDao.update(dataSourceForUpdate);

        // Validate
        Optional<DataSource> dataSourceOpt = dataSourceDao.findById(1L);
        assertTrue(dataSourceOpt.isPresent());
        DataSource dataSourceOfFetch = dataSourceOpt.get();
        assertThat(dataSourceOfFetch.getId(), is(1L));
        assertThat(dataSourceOfFetch.getName(), is("Hive"));

        HiveServerConnectionInfo connectionInfo = (HiveServerConnectionInfo) dataSourceOpt.get().getConnectionConfig().getUserConnection();
        assertThat(connectionInfo.getHost(), is("192.168.1.101"));
        assertThat(connectionInfo.getPort(), is(10001));
        assertThat(dataSourceOfFetch.getTags(), containsInAnyOrder("tag1", "tag2"));
        assertThat(dataSourceOfFetch.getDatasourceType(), is(DatasourceType.HIVE));
        assertThat(dataSourceOfFetch.getUpdateUser(), is("updater"));
        assertThat(dataSourceOfFetch.getUpdateTime(), is(updateTime));
    }

    @Test
    public void testDelete() {
        // Prepare
        ConnectionInfo hiveServerConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"127.0.0.1",10000);
        DataSource dataSource = MockDataSourceFactory.createDataSource(1L, "Hive", hiveServerConnectionInfo, DatasourceType.HIVE, Lists.newArrayList("test"));
        dataSourceDao.create(dataSource);

        // Execute
        dataSourceDao.delete(dataSource.getId());

        // Validate
        Optional<DataSource> dataSourceOpt = dataSourceDao.findById(1L);
        assertFalse(dataSourceOpt.isPresent());
    }

}
