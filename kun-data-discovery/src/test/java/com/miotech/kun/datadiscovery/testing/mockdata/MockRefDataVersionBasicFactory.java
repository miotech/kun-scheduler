package com.miotech.kun.datadiscovery.testing.mockdata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import com.miotech.kun.datadiscovery.model.enums.RefTableVersionStatus;
import lombok.SneakyThrows;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MockRefDataVersionBasicFactory {

    private MockRefDataVersionBasicFactory() {
    }

    @SneakyThrows
    public static MultipartFile getMultipartFile(String fileName, String filePath) {
        MultipartFile mulFile = new MockMultipartFile(fileName, fileName, "application/octet-stream", MockRefDataVersionBasicFactory.class.getResourceAsStream(filePath));
        return mulFile;
    }

    public static RefTableVersionInfo mockDefaultUnpublishedRefTableVersionInfo(Long versionId, Long tableId, String test_table, String test_database) {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefColumn refColumn1 = new RefColumn("fin_indicator_code", 0, "string");
        RefColumn refColumn2 = new RefColumn("name_zh_cn", 1, "string");
        RefColumn refColumn3 = new RefColumn("age", 2, "int");
        LinkedHashSet<RefColumn> refColumns = Sets.newLinkedHashSet();
        refColumns.add(refColumn1);
        refColumns.add(refColumn2);
        refColumns.add(refColumn3);
        RefTableVersionInfo info = new RefTableVersionInfo();
        info.setVersionId(versionId);
        info.setVersionNumber(null);
        info.setTableId(tableId);
        info.setVersionDescription("test");
        info.setTableName(test_table);
        info.setDatabaseName(test_database);
        info.setDataPath("test_path");
        info.setGlossaryList(glossaryList);
        info.setOwnerList(ownerList);
        info.setRefTableColumns(refColumns);
        info.setRefTableConstraints(map);
        info.setPublished(false);
        info.setStatus(RefTableVersionStatus.UNPUBLISHED);
        OffsetDateTime now = DateTimeUtils.now();
        info.setStartTime(null);
        info.setCreateUser("user1");
        info.setCreateTime(now);
        info.setUpdateUser("user1");
        info.setUpdateTime(now);
        info.setDeleted(false);
        info.setDatasetId(null);
        return info;
    }

    public static RefTableVersionInfo mockDefaultPublishedRefTableVersionInfo(Long versionId, Long tableId, Integer versionNumber, String test_table) {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefColumn refColumn1 = new RefColumn("fin_indicator_code", 0, "string");
        RefColumn refColumn2 = new RefColumn("name_zh_cn", 1, "string");
        RefColumn refColumn3 = new RefColumn("age", 2, "int");
        LinkedHashSet<RefColumn> refColumns = Sets.newLinkedHashSet();
        refColumns.add(refColumn1);
        refColumns.add(refColumn2);
        refColumns.add(refColumn3);
        RefTableVersionInfo info = new RefTableVersionInfo();
        info.setVersionId(versionId);
        info.setVersionNumber(versionNumber);
        info.setTableId(tableId);
        info.setVersionDescription("test");
        info.setTableName(test_table);
        info.setDatabaseName("test_database");
        info.setDataPath("test_path");
        info.setGlossaryList(glossaryList);
        info.setOwnerList(ownerList);
        info.setRefTableColumns(refColumns);
        info.setRefTableConstraints(map);
        info.setPublished(true);
        info.setStatus(RefTableVersionStatus.PUBLISHED);
        OffsetDateTime now = DateTimeUtils.now();
        info.setStartTime(now);
        info.setCreateUser("user1");
        info.setCreateTime(now);
        info.setUpdateUser("user1");
        info.setUpdateTime(now);
        info.setDeleted(false);
        info.setDatasetId(null);
        return info;
    }

    public static RefTableVersionInfo mockDefaultHistoryVersionInfo(Long versionId, Long tableId, Integer versionNumber) {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefColumn refColumn1 = new RefColumn("fin_indicator_code", 0, "string");
        RefColumn refColumn2 = new RefColumn("name_zh_cn", 1, "string");
        RefColumn refColumn3 = new RefColumn("age", 2, "int");
        LinkedHashSet<RefColumn> refColumns = Sets.newLinkedHashSet();
        refColumns.add(refColumn1);
        refColumns.add(refColumn2);
        refColumns.add(refColumn3);
        RefTableVersionInfo info = new RefTableVersionInfo();
        info.setVersionId(versionId);
        info.setVersionNumber(versionNumber);
        info.setTableId(tableId);
        info.setVersionDescription("test");
        info.setTableName("testTable");
        info.setDatabaseName("test_database");
        info.setDataPath("test_path");
        info.setGlossaryList(glossaryList);
        info.setOwnerList(ownerList);
        info.setRefTableColumns(refColumns);
        info.setRefTableConstraints(map);
        info.setPublished(true);
        info.setStatus(RefTableVersionStatus.PUBLISHED);
        OffsetDateTime now = DateTimeUtils.now();
        info.setStartTime(now);
        info.setCreateUser("user1");
        info.setCreateTime(now);
        info.setUpdateUser("user1");
        info.setUpdateTime(now);
        info.setDeleted(false);
        info.setDatasetId(null);
        return info;
    }


}
