package com.miotech.kun.datadiscovery.testing.rdm;

import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.enums.RefTableVersionStatus;
import com.miotech.kun.datadiscovery.persistence.RefTableVersionRepository;
import com.miotech.kun.datadiscovery.testing.DataDiscoveryTestBase;
import com.miotech.kun.datadiscovery.testing.mockdata.MockRefDataVersionBasicFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-30 15:04
 **/
public class RefTableVersionRepositoryTest extends DataDiscoveryTestBase {

    @Autowired
    RefTableVersionRepository refTableVersionRepository;

    @Test
    public void test_create() {
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(1L, 1L, "test_table", "test_database");
        refTableVersionRepository.create(info);
        RefTableVersionInfo refTableVersionInfo = refTableVersionRepository.findByVersionId(info.getVersionId());
        assertEquals(info, refTableVersionInfo);

    }


    @Test
    public void test_update() {
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(1L, 1L, "test_table", "test_database");
        refTableVersionRepository.create(info);
        RefTableVersionInfo refTableVersionInfo = refTableVersionRepository.findByVersionId(info.getVersionId());
        assertEquals(info, refTableVersionInfo);
        refTableVersionInfo.setTableName("table_update");
        refTableVersionRepository.update(refTableVersionInfo);
        RefTableVersionInfo refTableVersionInfo_update = refTableVersionRepository.findByVersionId(info.getVersionId());
        assertEquals(refTableVersionInfo, refTableVersionInfo_update);
    }

    @Test
    public void test_page_ref_table_info() {
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(1L, 1L, "test_table", "test_database");
        refTableVersionRepository.create(info);
        PageResult<RefTableVersionInfo> pageResult = refTableVersionRepository.pageRefTableInfo(1, 10);
        assertThat(pageResult.getTotalCount(), is(1));
        List<RefTableVersionInfo> records = pageResult.getRecords();
        assertThat(records.size(), is(1));
        RefTableVersionInfo refTableVersionInfo = records.get(0);
        assertEquals(refTableVersionInfo, info);
    }

    @Test
    public void test_list_ref_table_info() {
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(1L, 1L, "test_table", "test_database");
        refTableVersionRepository.create(info);
        List<RefTableVersionInfo> list = refTableVersionRepository.listRefTableInfoByIds(Collections.singletonList(1L));
        assertThat(list.size(), is(1));
        RefTableVersionInfo refTableVersionInfo = list.get(0);
        assertEquals(refTableVersionInfo, info);
    }

    @Test
    public void test_findPublishByTableId() {
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(1L, 1L, "test_table", "test_database");
        refTableVersionRepository.create(info);
        RefTableVersionInfo versionInfo = refTableVersionRepository.findPublishByTableId(info.getTableId());
        assertThat(versionInfo, is(nullValue()));
        RefTableVersionInfo publishedRefTableVersionInfo = MockRefDataVersionBasicFactory.mockDefaultPublishedRefTableVersionInfo(1L, 1L, 1, "test_table");
        refTableVersionRepository.update(publishedRefTableVersionInfo);
        RefTableVersionInfo versionInfo1 = refTableVersionRepository.findPublishByTableId(info.getTableId());
        assertThat(versionInfo1, is(notNullValue()));
        assertThat(versionInfo1.getVersionId(), is(publishedRefTableVersionInfo.getVersionId()));
        assertThat(versionInfo1.getTableId(), is(publishedRefTableVersionInfo.getTableId()));
        assertThat(versionInfo1.getPublished(), is(publishedRefTableVersionInfo.getPublished()));
        assertThat(versionInfo1.getStatus(), is(publishedRefTableVersionInfo.getStatus()));
    }

    @Test
    public void test_findByTableId() {
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(1L, 1L, "test_table", "test_database");
        refTableVersionRepository.create(info);
        List<RefTableVersionInfo> list = refTableVersionRepository.findByTableId(info.getTableId());
        assertThat(list.size(), is(1));
        RefTableVersionInfo refTableVersionInfo = list.get(0);
        assertThat(refTableVersionInfo.getVersionId(), is(info.getVersionId()));
    }

    @Test
    public void test_findMaxVersionNumberByTable() {
        Long versionId = 1L;
        Long tableId = 1L;
        Integer versionNumber = 1;
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(versionId, tableId, "test_table", "test_database");
        refTableVersionRepository.create(info);
        Integer vb = refTableVersionRepository.findMaxVersionNumberByTable(info.getTableId());
        assertThat(vb, is(nullValue()));
        RefTableVersionInfo info_publish = MockRefDataVersionBasicFactory.mockDefaultPublishedRefTableVersionInfo(versionId, tableId, versionNumber, "test_table");
        refTableVersionRepository.update(info_publish);
        Integer maxVersionNumber = refTableVersionRepository.findMaxVersionNumberByTable(info.getTableId());
        assertThat(maxVersionNumber, is(versionNumber));
    }

    @Test
    public void test_findUnPublishedByTable() {
        Long versionId = 1L;
        Long tableId = 1L;
        RefTableVersionInfo info = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(versionId, tableId, "test_table", "test_database");
        refTableVersionRepository.create(info);
        RefTableVersionInfo unPublishedByTable = refTableVersionRepository.findUnPublishedByTable(info.getTableId());
        assertThat(unPublishedByTable, is(notNullValue()));
        assertThat(unPublishedByTable.getVersionId(), is(versionId));
        assertThat(unPublishedByTable.getTableId(), is(tableId));
        assertThat(unPublishedByTable.getStatus(), is(RefTableVersionStatus.UNPUBLISHED));
        assertThat(unPublishedByTable.getPublished(), is(false));
        assertThat(unPublishedByTable.getStartTime(), is(nullValue()));
        assertThat(unPublishedByTable.getEndTime(), is(nullValue()));
        RefTableVersionInfo info_publish = MockRefDataVersionBasicFactory.mockDefaultPublishedRefTableVersionInfo(versionId, tableId, 1, "test_table");
        refTableVersionRepository.update(info_publish);
        RefTableVersionInfo unPublishedByTable2 = refTableVersionRepository.findUnPublishedByTable(info.getTableId());
        assertThat(unPublishedByTable2, is(nullValue()));

    }

    @Test
    public void test_existsDatabaseTableInfo() {
        Long versionId = 1L;
        Long tableId = 1L;
        String table1 = "test_table";
        String database1 = "test_database";
        RefTableVersionInfo info1 = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(versionId, tableId, table1, database1);
        refTableVersionRepository.create(info1);
        boolean exists1 = refTableVersionRepository.existsDatabaseTableInfo(database1, table1, null);
        assertThat(exists1, is(true));
        boolean exists2 = refTableVersionRepository.existsDatabaseTableInfo(database1, table1, tableId);
        assertThat(exists2, is(false));

    }

    @Test
    public void test_hasPublished() {
        Long versionId = 1L;
        Long tableId = 1L;
        String table1 = "test_table";
        String database1 = "test_database";
        RefTableVersionInfo info1 = MockRefDataVersionBasicFactory.mockDefaultUnpublishedRefTableVersionInfo(versionId, tableId, table1, database1);
        refTableVersionRepository.create(info1);
        boolean hasPublished = refTableVersionRepository.hasPublished(tableId);
        assertThat(hasPublished, is(false));
        RefTableVersionInfo publish_info = MockRefDataVersionBasicFactory.mockDefaultPublishedRefTableVersionInfo(versionId, tableId, 1, table1);
        refTableVersionRepository.update(publish_info);
        boolean hasPublished1 = refTableVersionRepository.hasPublished(tableId);
        assertThat(hasPublished1, is(true));

    }


}