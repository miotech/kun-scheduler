package com.miotech.kun.datadiscovery.testing;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.bo.EditRefDataTableRequest;
import com.miotech.kun.datadiscovery.model.bo.EditRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefTableMetaData;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefUpdateResult;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.datadiscovery.service.RdmService;
import com.miotech.kun.datadiscovery.service.RefTableVersionService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RdmServiceTest extends DataDiscoveryTestBase {

    @Autowired
    RdmService rdmService;
    @Autowired
    RefTableVersionService refTableVersionService;
    @Autowired
    GlossaryService glossaryService;
    @Value("${rdm.aws.bucket-name:test.ref.data}")
    private String bucketName;
    @Autowired
    private AmazonS3 amazonS3;


    @Test
    public void parse_CSVFile() {
        MultipartFile mulFile = getMultipartCSVFile("test.csv", "/test.csv");
        RefBaseTable refBaseTable = rdmService.parseFile(mulFile);
        assertThat(refBaseTable, is(notNullValue()));
        RefData refData = refBaseTable.getRefData();
        assertThat(refData, is(notNullValue()));
        assertThat(refData.getData(), is(notNullValue()));
        assertThat(refData.getData().size(), is(10));
        assertThat(refData.getHeaderMap().size(), is(4));
        RefTableMetaData refTableMetaData = refBaseTable.getRefTableMetaData();
        assertThat(refTableMetaData, is(notNullValue()));
        assertThat(refTableMetaData.getColumns().size(), is(4));


    }

    @SneakyThrows
    private MultipartFile getMultipartCSVFile(String fileName, String filePath) {
        MultipartFile mulFile = new MockMultipartFile(fileName, fileName, "application/octet-stream", this.getClass().getResourceAsStream(filePath));
        return mulFile;
    }

    @SneakyThrows
    @Test
    public void parse_EXECLFile() {
        MultipartFile mulFile = new MockMultipartFile("test.xlsx", "test.xlsx", "application/octet-stream", this.getClass().getResourceAsStream("/test.xlsx"));
        RefBaseTable refBaseTable = rdmService.parseFile(mulFile);
        assertThat(refBaseTable, is(notNullValue()));
        RefData refData = refBaseTable.getRefData();
        assertThat(refData, is(notNullValue()));
        assertThat(refData.getData(), is(notNullValue()));
        assertThat(refData.getData().size(), is(10));
        assertThat(refData.getHeaderMap().size(), is(4));
        RefTableMetaData refTableMetaData = refBaseTable.getRefTableMetaData();
        assertThat(refTableMetaData, is(notNullValue()));
        assertThat(refTableMetaData.getColumns().size(), is(4));
    }


    @Test
    public void init_version_ref_data_info() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefUpdateResult refUpdateResult = init_ref_table_version("test_table", glossaryList, ownerList, map);
        assertThat(refUpdateResult, is(notNullValue()));
        Assertions.assertTrue(refUpdateResult.isState());
        BaseRefTableVersionInfo baseRefTableVersionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        assertThat(baseRefTableVersionInfo, is(notNullValue()));
        Long updateVersionId = baseRefTableVersionInfo.getVersionId();
        assertThat(updateVersionId, is(notNullValue()));
        RefTableVersionInfo refVersionInfo = refTableVersionService.getRefVersionInfo(updateVersionId);
        assertThat(refVersionInfo.getTableId(), is(notNullValue()));
        assertThat(refVersionInfo.getPublished(), is(false));
        assertThat(refVersionInfo.getVersionNumber(), is(1));
        assertThat(refVersionInfo.getGlossaryList().size(), is(glossaryList.size()));
        assertThat(refVersionInfo.getOwnerList().size(), is(ownerList.size()));
        assertThat(refVersionInfo.getRefTableColumns().size(), is(4));
        assertThat(refVersionInfo.getRefTableConstraints().size(), is(map.size()));
        assertThat(refVersionInfo.getVersionNumber(), is(1));
        assertThat(refVersionInfo.getStartTime(), is(nullValue()));
        assertThat(refVersionInfo.getEndTime(), is(nullValue()));
        assertThat(refVersionInfo.getCreateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getCreateTime(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateTime(), is(notNullValue()));
        assertThat(refVersionInfo.isDeleted(), is(false));
    }

    @SneakyThrows
    @Test
    public void ref_data_read_write() {
        String fileName = "test.csv";
        String filePath = "/test.csv";
        MultipartFile multipartCSVFile = getMultipartCSVFile(fileName, filePath);
        RefBaseTable refBaseTable = rdmService.parseFile(multipartCSVFile);
        Long tableId = null;
        Long versionId = null;
        String desc = "test_desc";
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));

        RefTableMetaData refTableMetaData = refBaseTable.getRefTableMetaData();
        refTableMetaData.setRefTableConstraints(map);
        refBaseTable.setRefTableMetaData(refTableMetaData);
        EditRefTableVersionInfo editRefVersionInfo = new EditRefTableVersionInfo("test_table", desc, glossaryList, ownerList);
        EditRefDataTableRequest request = new EditRefDataTableRequest();
        request.setRefBaseTable(refBaseTable);
        request.setTableId(tableId);
        request.setVersionId(versionId);
        request.setEditRefTableVersionInfo(editRefVersionInfo);
        RefUpdateResult refUpdateResult = rdmService.updateUnpublishedRefDataInfo(request);
        assertThat(refUpdateResult, is(notNullValue()));
        Assertions.assertTrue(refUpdateResult.isState());
        BaseRefTableVersionInfo baseRefTableVersionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        assertThat(baseRefTableVersionInfo, is(notNullValue()));
        Long updateVersionId = baseRefTableVersionInfo.getVersionId();
        assertThat(updateVersionId, is(notNullValue()));
        RefTableVersionInfo refVersionInfo = refTableVersionService.getRefVersionInfo(updateVersionId);
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(multipartCSVFile.getInputStream());
        Mockito.when(amazonS3.getObject(bucketName, refVersionInfo.getDataPath())).thenReturn(s3Object);

        RefBaseTable refBaseTableResult = rdmService.fetchVersionData(refVersionInfo.getVersionId());
        assertThat(JSONUtils.toJsonString(refBaseTable), is(JSONUtils.toJsonString(refBaseTableResult)));
    }

    @Test
    public void edit_version_ref_data_info() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefUpdateResult refUpdateResult = init_ref_table_version("test_table", glossaryList, ownerList, map);
//        update
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        String fileName_update = "test_update.csv";
        String tableName_update = "test_table_update";
        String filePath_udpate = "/test_update.csv";
        String desc_update = "test_desc_update";
        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        Long tableId_update = versionInfo.getTableId();
        Long versionId_update = versionInfo.getVersionId();
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId_update, versionId_update, fileName_update, filePath_udpate, tableName_update, desc_update, glossaryList_update, ownerList_update, map);
        RefUpdateResult refUpdateResultUpdate = rdmService.updateUnpublishedRefDataInfo(request_update);
        assertThat(refUpdateResultUpdate, is(notNullValue()));
        Assertions.assertTrue(refUpdateResultUpdate.isState());
        BaseRefTableVersionInfo baseRefTableVersionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        assertThat(baseRefTableVersionInfo, is(notNullValue()));
        Long updateVersionId = baseRefTableVersionInfo.getVersionId();
        assertThat(updateVersionId, is(versionId_update));
        RefTableVersionInfo refVersionInfo = refTableVersionService.getRefVersionInfo(updateVersionId);
        assertThat(refVersionInfo.getTableId(), is(tableId_update));
        assertThat(refVersionInfo.getPublished(), is(false));
        assertThat(refVersionInfo.getVersionNumber(), is(1));
        assertThat(refVersionInfo.getGlossaryList().size(), is(glossaryList_update.size()));
        assertThat(refVersionInfo.getOwnerList().size(), is(ownerList_update.size()));
        assertThat(refVersionInfo.getRefTableColumns().size(), is(3));
        assertThat(refVersionInfo.getRefTableConstraints().size(), is(map.size()));
        assertThat(refVersionInfo.getStartTime(), is(nullValue()));
        assertThat(refVersionInfo.getEndTime(), is(nullValue()));
        assertThat(refVersionInfo.getCreateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getCreateTime(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateTime(), is(notNullValue()));
        assertThat(refVersionInfo.isDeleted(), is(false));
        rdmService.activateRefDataTableVersion(updateVersionId);
        RefTableVersionInfo refVersionInfoPublish = refTableVersionService.getRefVersionInfo(updateVersionId);
        assertThat(refVersionInfoPublish.getTableId(), is(tableId_update));
        assertThat(refVersionInfoPublish.getPublished(), is(true));
        assertThat(refVersionInfoPublish.getVersionNumber(), is(1));
        assertThat(refVersionInfoPublish.getGlossaryList().size(), is(glossaryList_update.size()));
        assertThat(refVersionInfoPublish.getOwnerList().size(), is(ownerList_update.size()));
        assertThat(refVersionInfoPublish.getRefTableColumns().size(), is(3));
        assertThat(refVersionInfoPublish.getRefTableConstraints().size(), is(map.size()));
        assertThat(refVersionInfoPublish.getStartTime(), is(notNullValue()));
        assertThat(refVersionInfoPublish.getEndTime(), is(nullValue()));
        assertThat(refVersionInfoPublish.getCreateUser(), is(notNullValue()));
        assertThat(refVersionInfoPublish.getCreateTime(), is(notNullValue()));
        assertThat(refVersionInfoPublish.getUpdateUser(), is(notNullValue()));
        assertThat(refVersionInfoPublish.getUpdateTime(), is(notNullValue()));
        assertThat(refVersionInfoPublish.isDeleted(), is(false));

    }

    @Test
    public void test_activate_ref_data_table_version() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefUpdateResult refUpdateResult = init_ref_table_version("test_table", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long versionId = versionInfo.getVersionId();
        boolean activate = rdmService.activateRefDataTableVersion(versionId);
        Assertions.assertTrue(activate);
        RefTableVersionInfo refVersionInfo = refTableVersionService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo.getPublished(), is(true));
        assertThat(refVersionInfo.getVersionNumber(), is(1));
        assertThat(refVersionInfo.getGlossaryList().size(), is(glossaryList.size()));
        assertThat(refVersionInfo.getOwnerList().size(), is(ownerList.size()));
        assertThat(refVersionInfo.getRefTableColumns().size(), is(4));
        assertThat(refVersionInfo.getRefTableConstraints().size(), is(map.size()));
        assertThat(refVersionInfo.getStartTime(), is(notNullValue()));
        assertThat(refVersionInfo.getEndTime(), is(nullValue()));
        assertThat(refVersionInfo.getCreateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getCreateTime(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateTime(), is(notNullValue()));
        assertThat(refVersionInfo.isDeleted(), is(false));
    }

    @Test
    public void test_activate_ref_data_table_version_throw_repeat_published() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefUpdateResult refUpdateResult = init_ref_table_version("test_table", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long versionId = versionInfo.getVersionId();
        rdmService.activateRefDataTableVersion(versionId);
        Exception ex1 = assertThrows(IllegalStateException.class, () -> rdmService.activateRefDataTableVersion(versionId));
        assertEquals(String.format("version is published,version number:%s", versionInfo.getVersionNumber()), ex1.getMessage());
    }

    @Test
    public void test_deactivate_ref_data_table_version() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = new LinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefUpdateResult refUpdateResult = init_ref_table_version("test_table", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long versionId = versionInfo.getVersionId();
        rdmService.activateRefDataTableVersion(versionId);
        boolean deactivate = rdmService.deactivateRefDataTableVersion(versionId);
        Assertions.assertTrue(deactivate);
        RefTableVersionInfo refVersionInfo = refTableVersionService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo.getPublished(), is(false));
        assertThat(refVersionInfo.getVersionNumber(), is(1));
        assertThat(refVersionInfo.getStartTime(), is(notNullValue()));
        assertThat(refVersionInfo.getEndTime(), is(notNullValue()));
        assertThat(refVersionInfo.isDeleted(), is(false));
    }


    private RefUpdateResult init_ref_table_version(String tableName, List<Long> glossaryList, List<String> ownerList, LinkedHashMap<ConstraintType, Set<String>> map) {
        Long tableId = null;
        Long versionId = null;
        String fileName = "test.csv";
        String filePath = "/test.csv";
        String desc = "test_desc";
        EditRefDataTableRequest request = mockEditRefVersionInfo(tableId, versionId, fileName, filePath, tableName, desc, glossaryList, ownerList, map);
        RefUpdateResult refUpdateResult = rdmService.updateUnpublishedRefDataInfo(request);
        return refUpdateResult;
    }

    private EditRefDataTableRequest mockEditRefVersionInfo(Long tableId, Long versionId, String fileName, String filePath, String tableName, String desc, List<Long> glossaryList, List<String> ownerList, LinkedHashMap<ConstraintType, Set<String>> map) {

        RefBaseTable refBaseTable = rdmService.parseFile(getMultipartCSVFile(fileName, filePath));
        RefTableMetaData refTableMetaData = refBaseTable.getRefTableMetaData();
        refTableMetaData.setRefTableConstraints(map);
        refBaseTable.setRefTableMetaData(refTableMetaData);
        EditRefTableVersionInfo editRefVersionInfo = new EditRefTableVersionInfo(tableName, desc, glossaryList, ownerList);
        EditRefDataTableRequest request = new EditRefDataTableRequest();
        request.setRefBaseTable(refBaseTable);
        request.setTableId(tableId);
        request.setVersionId(versionId);
        request.setEditRefTableVersionInfo(editRefVersionInfo);
        return request;
    }


}