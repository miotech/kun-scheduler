package com.miotech.kun.metadata.common.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.core.model.datasource.DataSourceBasicInfo;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceSearchFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5432);
        DataSourceBasicInfo dataSourceBasicInfo = MockDataSourceFactory.createDataSourceBasicInfo(1L, "Hive", hostPortDatasourceConfig, DatasourceType.HIVE, Lists.newArrayList("test"), "admin", "admin");
        dataSourceDao.create(dataSourceBasicInfo);
        // Execute
        Optional<DataSourceBasicInfo> existDataSourceOpt = dataSourceDao.findById(1L);
        Optional<DataSourceBasicInfo> nonExistDataSourceOpt = dataSourceDao.findById(2L);

        // Validate
        Assertions.assertTrue(existDataSourceOpt.isPresent());
        DataSourceBasicInfo existDataSourceBasicInfo = existDataSourceOpt.get();
        assertThat(existDataSourceBasicInfo.getId(), is(1L));
        assertThat(existDataSourceBasicInfo.getName(), is("Hive"));
        assertThat(existDataSourceBasicInfo.getTags(), containsInAnyOrder("test"));
        assertThat(existDataSourceBasicInfo.getDatasourceType(), is(DatasourceType.HIVE));
        assertThat(existDataSourceBasicInfo.getDatasourceConfigInfo().size(), is(dataSourceBasicInfo.getDatasourceConfigInfo().size()));

        Assertions.assertFalse(nonExistDataSourceOpt.isPresent());
    }

    @Test
    public void testFetchTotalCountWithFilter() {
        // Prepare
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        DataSourceBasicInfo dataSourceBasicInfo = MockDataSourceFactory.createDataSourceBasicInfo(1L, "Hive", hostPortDatasourceConfig, DatasourceType.HIVE, Lists.newArrayList("test"), "admin", "admin");
        dataSourceDao.create(dataSourceBasicInfo);

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
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        DataSourceBasicInfo dataSourceBasicInfo = MockDataSourceFactory.createDataSourceBasicInfo(1L, "Hive", hostPortDatasourceConfig, DatasourceType.HIVE, Lists.newArrayList("test"), "admin", "admin");
        dataSourceDao.create(dataSourceBasicInfo);

        // Execute
        List<DataSourceBasicInfo> shouldExistDataSources = dataSourceDao.fetchWithFilter(new DataSourceSearchFilter("Hive", 1, 20));
        List<DataSourceBasicInfo> nonExistDataSources = dataSourceDao.fetchWithFilter(new DataSourceSearchFilter("HBase", 1, 20));

        // Validate
        assertThat(shouldExistDataSources.size(), is(1));
        DataSourceBasicInfo dataSourceOfFetch = shouldExistDataSources.get(0);
        assertThat(dataSourceOfFetch.getId(), is(1L));
        assertThat(dataSourceOfFetch.getName(), is("Hive"));
        assertThat(dataSourceOfFetch.getTags(), containsInAnyOrder("test"));
        assertThat(dataSourceOfFetch.getDatasourceType(), is(DatasourceType.HIVE));

        assertThat(nonExistDataSources.size(), is(0));
    }

    @Test
    public void testCreate() {
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        DataSourceBasicInfo dataSourceBasicInfo = MockDataSourceFactory.createDataSourceBasicInfo(1L, "Hive", hostPortDatasourceConfig, DatasourceType.HIVE, Lists.newArrayList("test"), "admin", "admin");
        dataSourceDao.create(dataSourceBasicInfo);
        // Validate
        Optional<DataSourceBasicInfo> dataSourceOpt = dataSourceDao.findById(1L);


        Assertions.assertTrue(dataSourceOpt.isPresent());
        DataSourceBasicInfo dataSourceOfFetch = dataSourceOpt.get();
        assertThat(dataSourceOfFetch.getId(), is(1L));
        assertThat(dataSourceOfFetch.getName(), is("Hive"));

        Map<String, Object> datasourceConfigInfoOpt = dataSourceOpt.get().getDatasourceConfigInfo();
        assertThat(datasourceConfigInfoOpt.get("host"), is("127.0.0.1"));
        assertThat(datasourceConfigInfoOpt.get("port"), is(10000));
        assertThat(dataSourceOfFetch.getTags(), containsInAnyOrder("test"));
        assertThat(dataSourceOfFetch.getDatasourceType(), is(DatasourceType.HIVE));
    }

    @Test
    public void testUpdate() {
        // Prepare
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        DataSourceBasicInfo dataSourceBasicInfo = MockDataSourceFactory.createDataSourceBasicInfo(1L, "Hive", hostPortDatasourceConfig, DatasourceType.HIVE, Lists.newArrayList("test"), "admin", "admin");
        dataSourceDao.create(dataSourceBasicInfo);
        OffsetDateTime now = DateTimeUtils.now();
        // Execute
        OffsetDateTime updateTime = now.minusMinutes(1);
        Map<String, Object> hostPortDatasourceConfigNew = MockDataSourceFactory.createHostPortDatasourceConfig("192.168.1.101", 10001);
        DataSourceBasicInfo dataSourceForUpdate = dataSourceBasicInfo.cloneBuilder()
                .withDatasourceConfigInfo(hostPortDatasourceConfigNew)
                .withUpdateUser("updater")
                .withUpdateTime(updateTime)
                .withTags(Lists.newArrayList("tag1", "tag2"))
                .build();
        dataSourceDao.update(dataSourceForUpdate);

        // Validate
        Optional<DataSourceBasicInfo> dataSourceOpt = dataSourceDao.findById(1L);
        assertTrue(dataSourceOpt.isPresent());
        DataSourceBasicInfo dataSourceOfFetch = dataSourceOpt.get();
        assertThat(dataSourceOfFetch.getId(), is(1L));
        assertThat(dataSourceOfFetch.getName(), is("Hive"));
        Map<String, Object> datasourceConfigInfoOpt = dataSourceOpt.get().getDatasourceConfigInfo();
        assertThat(datasourceConfigInfoOpt.get("host"), is(hostPortDatasourceConfigNew.get("host")));
        assertThat(datasourceConfigInfoOpt.get("port"), is(hostPortDatasourceConfigNew.get("port")));
        assertThat(dataSourceOfFetch.getTags(), containsInAnyOrder("tag1", "tag2"));
        assertThat(dataSourceOfFetch.getDatasourceType(), is(DatasourceType.HIVE));
        assertThat(dataSourceOfFetch.getUpdateUser(), is("updater"));
        assertThat(dataSourceOfFetch.getUpdateTime(), is(updateTime));
    }

    @Test
    public void testDelete() {
        // Prepare
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        DataSourceBasicInfo dataSourceBasicInfo = MockDataSourceFactory.createDataSourceBasicInfo(1L, "Hive", hostPortDatasourceConfig, DatasourceType.HIVE, Lists.newArrayList("test"), "admin", "admin");
        dataSourceDao.create(dataSourceBasicInfo);

        // Execute
        dataSourceDao.delete(dataSourceBasicInfo.getId());

        // Validate
        Optional<DataSourceBasicInfo> dataSourceOpt = dataSourceDao.findById(1L);
        assertFalse(dataSourceOpt.isPresent());
    }


}
