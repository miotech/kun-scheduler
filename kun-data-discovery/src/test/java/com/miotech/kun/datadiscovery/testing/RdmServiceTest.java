package com.miotech.kun.datadiscovery.testing;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.miotech.kun.common.model.PageRequest;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.bo.EditRefDataTableRequest;
import com.miotech.kun.datadiscovery.model.bo.EditRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefTableMetaData;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefUpdateResult;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import com.miotech.kun.datadiscovery.model.enums.RefTableVersionStatus;
import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.vo.RefDataVersionFillInfo;
import com.miotech.kun.datadiscovery.model.vo.RefTableVersionFillInfo;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.datadiscovery.service.LineageAppService;
import com.miotech.kun.datadiscovery.service.RdmService;
import com.miotech.kun.datadiscovery.service.rdm.file.S3StorageFileManger;
import com.miotech.kun.metadata.core.model.event.DatasetCreatedEvent;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.model.UserInfo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.miotech.kun.datadiscovery.testing.mockdata.MockRefDataVersionBasicFactory.getMultipartCSVFile;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RdmServiceTest extends DataDiscoveryTestBase {

    @SpyBean
    RdmService rdmService;
    @MockBean
    GlossaryService glossaryService;
    @MockBean
    LineageAppService lineageAppService;
    @Value("${rdm.aws.bucket-name:test.ref.data}")
    private String bucketName;
    @Value("${rdm.datasource:0}")
    private Long datasourceId;
    @Autowired
    private AmazonS3 amazonS3;


    @Test
    public void parse_csv_file() {
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
    @Test
    public void parse_execl_file() {
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
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        RefUpdateResult refUpdateResult = init_create_ref_table_version("test_table_init", "test_database", glossaryList, ownerList, map);
        assertThat(refUpdateResult, is(notNullValue()));
        Assertions.assertTrue(refUpdateResult.isState());
        BaseRefTableVersionInfo baseRefTableVersionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        assertThat(baseRefTableVersionInfo, is(notNullValue()));
        Long updateVersionId = baseRefTableVersionInfo.getVersionId();
        assertThat(updateVersionId, is(notNullValue()));
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(updateVersionId);
        assertThat(refVersionInfo.getTableId(), is(notNullValue()));
        assertThat(refVersionInfo.getPublished(), is(false));
        assertThat(refVersionInfo.getStatus(), is(RefTableVersionStatus.UNPUBLISHED));
        assertThat(refVersionInfo.getVersionNumber(), is(nullValue()));
        assertThat(refVersionInfo.getGlossaryList().size(), is(glossaryList.size()));
        assertThat(refVersionInfo.getOwnerList().size(), is(ownerList.size()));
        assertThat(refVersionInfo.getRefTableColumns().size(), is(4));
        assertThat(refVersionInfo.getRefTableConstraints().size(), is(map.size()));
        assertThat(refVersionInfo.getVersionNumber(), is(nullValue()));
        assertThat(refVersionInfo.getStartTime(), is(nullValue()));
        assertThat(refVersionInfo.getEndTime(), is(nullValue()));
        assertThat(refVersionInfo.getCreateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getCreateTime(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateTime(), is(notNullValue()));
        assertThat(refVersionInfo.isDeleted(), is(false));
    }

    @Test
    public void ref_data_read_write() {
        String fileName = "test.csv";
        String filePath = "/test.csv";
        MultipartFile multipartCSVFile = getMultipartCSVFile(fileName, filePath);
        RefBaseTable refBaseTable = rdmService.parseFile(multipartCSVFile);
        String desc = "test_desc";
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        RefTableMetaData refTableMetaData = refBaseTable.getRefTableMetaData();
        refTableMetaData.setRefTableConstraints(map);
        refBaseTable.setRefTableMetaData(refTableMetaData);
        EditRefTableVersionInfo editRefVersionInfo = new EditRefTableVersionInfo("test_table", "test_database", desc, glossaryList, ownerList, refTableMetaData);
        EditRefDataTableRequest request = new EditRefDataTableRequest();
        request.setRefBaseTable(refBaseTable);
        request.setEditRefTableVersionInfo(editRefVersionInfo);
//        first edit
        RefUpdateResult refUpdateResult = rdmService.editRefDataInfo(request);
        assertThat(refUpdateResult, is(notNullValue()));
        Assertions.assertTrue(refUpdateResult.isState());
        BaseRefTableVersionInfo baseRefTableVersionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        assertThat(baseRefTableVersionInfo, is(notNullValue()));
        Long versionId = baseRefTableVersionInfo.getVersionId();
        Long tableId = baseRefTableVersionInfo.getTableId();
        assertThat(versionId, is(notNullValue()));
        assertThat(tableId, is(notNullValue()));
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        mockS3Object("test_s3.csv", "/test_s3.csv", refVersionInfo);
        RefDataVersionFillInfo refDataVersionFillInfo = rdmService.fetchRefDataVersionInfo(refVersionInfo.getVersionId());

        RefBaseTable refBaseTable1 = refDataVersionFillInfo.getRefBaseTable();
        assertThat(refBaseTable.getRefData().getData().size(), is(refBaseTable1.getRefData().getData().size()));
        assertThat(refBaseTable.getRefData().getHeaderMap().size(), is(refBaseTable1.getRefData().getHeaderMap().size()));
        assertThat(refBaseTable.getRefTableMetaData().getRefTableConstraints().size(), is(refBaseTable1.getRefTableMetaData().getRefTableConstraints().size()));
        assertThat(refBaseTable.getRefTableMetaData().getColumns().size(), is(refBaseTable1.getRefTableMetaData().getColumns().size()));

    }

    @SneakyThrows
    private void mockS3Object(String fileName, String filePath, RefTableVersionInfo refVersionInfo) {
        S3Object s3Object = new S3Object();
        MultipartFile multipartCSVFile = getMultipartCSVFile(fileName, filePath);
        s3Object.setObjectContent(multipartCSVFile.getInputStream());
        when(amazonS3.getObject(bucketName, S3StorageFileManger.relativePath(refVersionInfo.getDataPath()))).thenReturn(s3Object);
    }


    @Test
    public void edit_version_ref_data_info() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        map.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("fin_indicator_code", "name_zh_cn"));
        RefUpdateResult refUpdateResult = init_create_ref_table_version("test_table", "test_database", glossaryList, ownerList, map);
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
        String database = "test_database";
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId_update, versionId_update, fileName_update, filePath_udpate, tableName_update, database, desc_update, glossaryList_update, ownerList_update, map);
        RefUpdateResult refUpdateResultUpdate = rdmService.editRefDataInfo(request_update);
        assertThat(refUpdateResultUpdate, is(notNullValue()));
        Assertions.assertTrue(refUpdateResultUpdate.isState());
        BaseRefTableVersionInfo baseRefTableVersionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        assertThat(baseRefTableVersionInfo, is(notNullValue()));
        Long updateVersionId = baseRefTableVersionInfo.getVersionId();
        assertThat(updateVersionId, is(versionId_update));
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(updateVersionId);
        assertThat(refVersionInfo.getTableId(), is(tableId_update));
        assertThat(refVersionInfo.getPublished(), is(false));
        assertThat(refVersionInfo.getStatus(), is(RefTableVersionStatus.UNPUBLISHED));
        assertThat(refVersionInfo.getVersionNumber(), is(nullValue()));
        assertThat(refVersionInfo.getGlossaryList().size(), is(glossaryList_update.size()));
        assertThat(refVersionInfo.getOwnerList().size(), is(ownerList_update.size()));
        assertThat(refVersionInfo.getRefTableColumns().size(), is(4));
        assertThat(refVersionInfo.getRefTableConstraints().size(), is(map.size()));
        assertThat(refVersionInfo.getStartTime(), is(nullValue()));
        assertThat(refVersionInfo.getEndTime(), is(nullValue()));
        assertThat(refVersionInfo.getCreateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getCreateTime(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateUser(), is(notNullValue()));
        assertThat(refVersionInfo.getUpdateTime(), is(notNullValue()));
        assertThat(refVersionInfo.isDeleted(), is(false));
    }

    @Test
    public void next_version_ref_data_info() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        RefUpdateResult refUpdateResult = init_create_ref_table_version("test_table_test", "test_database", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        //        publish
        rdmService.publishRefDataTableVersion(versionId);
        RefTableVersionInfo refVersionInfoPublish = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfoPublish.getTableId(), is(tableId));
        assertThat(refVersionInfoPublish.getVersionNumber(), is(1));
        assertThat(refVersionInfoPublish.getVersionId(), is(versionId));
        assertThat(refVersionInfoPublish.getPublished(), is(true));
        assertThat(refVersionInfoPublish.getStatus(), is(RefTableVersionStatus.PUBLISHED));
        assertThat(refVersionInfoPublish.getStartTime(), is(notNullValue()));

//        update published verison
        String fileName_update = "test_update.csv";
        String tableName_update = "test_table_update";
        String filePath_udpate = "/test_update.csv";
        String desc_update = "test_desc_update";
        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        String database = "test_database";
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId, versionId, fileName_update, filePath_udpate, tableName_update, database, desc_update, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult = rdmService.editRefDataInfo(request_update);
        BaseRefTableVersionInfo updateVersionInfo = updateResult.getBaseRefTableVersionInfo();
        RefTableVersionInfo refVersionInfoUpdate = rdmService.getRefVersionInfo(updateVersionInfo.getVersionId());
        assertThat(refVersionInfoUpdate.getTableId(), is(tableId));
        assertThat(refVersionInfoUpdate.getPublished(), is(false));
        assertThat(refVersionInfoUpdate.getStatus(), is(RefTableVersionStatus.UNPUBLISHED));
        assertThat(refVersionInfoUpdate.getVersionNumber(), is(nullValue()));
        assertThat(refVersionInfoUpdate.getGlossaryList().size(), is(glossaryList_update.size()));
        assertThat(refVersionInfoUpdate.getOwnerList().size(), is(ownerList_update.size()));
        assertThat(refVersionInfoUpdate.getRefTableColumns().size(), is(4));
        assertThat(refVersionInfoUpdate.getRefTableConstraints().size(), is(map.size()));
        assertThat(refVersionInfoUpdate.getStartTime(), is(nullValue()));
        assertThat(refVersionInfoUpdate.getEndTime(), is(nullValue()));
        assertThat(refVersionInfoUpdate.getCreateUser(), is(notNullValue()));
        assertThat(refVersionInfoUpdate.getCreateTime(), is(notNullValue()));
        assertThat(refVersionInfoUpdate.getUpdateUser(), is(notNullValue()));
        assertThat(refVersionInfoUpdate.getUpdateTime(), is(notNullValue()));
        assertThat(refVersionInfoUpdate.isDeleted(), is(false));
    }

    @Test
    public void test_activate_ref_data_table_version() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        RefUpdateResult refUpdateResult = init_create_ref_table_version("test_table", "test_database", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long versionId = versionInfo.getVersionId();
        boolean activate = rdmService.publishRefDataTableVersion(versionId);
        Assertions.assertTrue(activate);
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo.getPublished(), is(true));
        assertThat(refVersionInfo.getStatus(), is(RefTableVersionStatus.PUBLISHED));
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
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        RefUpdateResult refUpdateResult = init_create_ref_table_version("test_table", "test_database", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long versionId = versionInfo.getVersionId();
        rdmService.publishRefDataTableVersion(versionId);
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        Exception ex1 = assertThrows(IllegalStateException.class, () -> rdmService.publishRefDataTableVersion(versionId));
        Assertions.assertEquals(String.format("version is published,version number:V%s", refVersionInfo.getVersionNumber()), ex1.getMessage());
        rdmService.deactivateRefDataTableVersion(versionId);
        RefTableVersionInfo refVersionInfo1 = rdmService.getRefVersionInfo(versionId);
        Exception ex2 = assertThrows(IllegalStateException.class, () -> rdmService.publishRefDataTableVersion(versionId));
        Assertions.assertEquals(String.format("Historical versions cannot be published directly，version number:V%s", refVersionInfo1.getVersionNumber()), ex2.getMessage());
    }

    @Test
    public void test_rollback_refdata_table_version() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        RefUpdateResult refUpdateResult = init_create_ref_table_version("test_table", "test_database", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long versionId = versionInfo.getVersionId();
        //        回滚未发布的
        Exception ex1 = assertThrows(IllegalStateException.class, () -> rdmService.rollbackRefDataTableVersion(versionId));
        Assertions.assertEquals("Rollback is not supported for non historical versions，version number:V-", ex1.getMessage());

        rdmService.publishRefDataTableVersion(versionId);
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo.getVersionNumber(), is(1));
        assertThat(refVersionInfo.getStatus(), is(RefTableVersionStatus.PUBLISHED));
        assertThat(refVersionInfo.getPublished(), is(true));
        assertThat(refVersionInfo.getStartTime(), is(notNullValue()));
        assertThat(refVersionInfo.getEndTime(), is(nullValue()));
        //        回滚发布的
        Exception ex2 = assertThrows(IllegalStateException.class, () -> rdmService.rollbackRefDataTableVersion(versionId));
        Assertions.assertEquals(String.format("Rollback is not supported for non historical versions，version number:V%s", refVersionInfo.getVersionNumber()), ex2.getMessage());
        rdmService.deactivateRefDataTableVersion(versionId);
        RefTableVersionInfo deactivateRefDataTableVersion = rdmService.getRefVersionInfo(versionId);
        assertThat(deactivateRefDataTableVersion.getVersionNumber(), is(1));
        assertThat(deactivateRefDataTableVersion.getStatus(), is(RefTableVersionStatus.HISTORY));
        assertThat(deactivateRefDataTableVersion.getPublished(), is(false));
        assertThat(deactivateRefDataTableVersion.getStartTime(), is(notNullValue()));
        assertThat(deactivateRefDataTableVersion.getEndTime(), is(notNullValue()));

        //        回滚历史版本；
        rdmService.rollbackRefDataTableVersion(versionId);
        RefTableVersionInfo rollBackRefDataTableVersion = rdmService.getRefVersionInfo(versionId);
        assertThat(rollBackRefDataTableVersion.getVersionNumber(), is(1));
        assertThat(rollBackRefDataTableVersion.getStartTime(), is(notNullValue()));
        assertThat(rollBackRefDataTableVersion.getEndTime(), is(nullValue()));
        assertThat(rollBackRefDataTableVersion.getStatus(), is(RefTableVersionStatus.PUBLISHED));
        assertThat(rollBackRefDataTableVersion.getPublished(), is(true));
    }

    @Test
    public void test_deactivate_ref_data_table_version() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L, 3L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        RefUpdateResult refUpdateResult = init_create_ref_table_version("test_table", "test_database", glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long versionId = versionInfo.getVersionId();
        //        停用未发布的
        Exception ex1 = assertThrows(IllegalStateException.class, () -> rdmService.deactivateRefDataTableVersion(versionId));
        Assertions.assertEquals("version is unpublished,version number:null", ex1.getMessage());
        rdmService.publishRefDataTableVersion(versionId);
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo.getVersionNumber(), is(1));
        assertThat(refVersionInfo.getStatus(), is(RefTableVersionStatus.PUBLISHED));
        assertThat(refVersionInfo.getPublished(), is(true));
        assertThat(refVersionInfo.getStartTime(), is(notNullValue()));
        assertThat(refVersionInfo.getEndTime(), is(nullValue()));
        //        停用发布的
        rdmService.deactivateRefDataTableVersion(versionId);
        RefTableVersionInfo deactivateRefDataTableVersion = rdmService.getRefVersionInfo(versionId);
        assertThat(deactivateRefDataTableVersion.getVersionNumber(), is(1));
        assertThat(deactivateRefDataTableVersion.getStatus(), is(RefTableVersionStatus.HISTORY));
        assertThat(deactivateRefDataTableVersion.getPublished(), is(false));
        assertThat(deactivateRefDataTableVersion.getStartTime(), is(notNullValue()));
        assertThat(deactivateRefDataTableVersion.getEndTime(), is(notNullValue()));
        //        停用历史版本；
        Exception ex2 = assertThrows(IllegalStateException.class, () -> rdmService.deactivateRefDataTableVersion(versionId));
        Assertions.assertEquals("version is unpublished,version number:1", ex2.getMessage());

    }


    @Test
    public void test_fetch_editable_refdata_version_info() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        mockS3Object("test_s3.csv", "/test_s3.csv", refVersionInfo);

        RefDataVersionFillInfo refDataVersionFillInfo = rdmService.fetchEditableRefDataVersionInfo(tableId);
        assertThat(refDataVersionFillInfo.getVersionId(), is(versionId));
        assertThat(refDataVersionFillInfo.getTableId(), is(tableId));
        assertThat(refDataVersionFillInfo.getVersionNumber(), is(nullValue()));
        assertThat(refDataVersionFillInfo.getPublished(), is(false));
        assertThat(refDataVersionFillInfo.getShowStatus(), is(RefTableVersionStatus.UNPUBLISHED));
        assertThat(refDataVersionFillInfo.getStartTime(), is(nullValue()));
        assertThat(refDataVersionFillInfo.getEndTime(), is(nullValue()));
        rdmService.publishRefDataTableVersion(versionId);
        RefDataVersionFillInfo publishRefDataTableVersion = rdmService.fetchEditableRefDataVersionInfo(tableId);
        assertThat(publishRefDataTableVersion.getVersionId(), is(versionId));
        assertThat(publishRefDataTableVersion.getTableId(), is(tableId));
        assertThat(publishRefDataTableVersion.getVersionNumber(), is(1));
        assertThat(publishRefDataTableVersion.getPublished(), is(true));
        assertThat(publishRefDataTableVersion.getShowStatus(), is(RefTableVersionStatus.PUBLISHED));
        assertThat(publishRefDataTableVersion.getStartTime(), is(notNullValue()));
        assertThat(publishRefDataTableVersion.getEndTime(), is(nullValue()));
        //        update published verison
        String fileName_update = "test_update.csv";
        String tableName_update = "test_table_update";
        String filePath_udpate = "/test_update.csv";
        String desc_update = "test_desc_update";
        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId, versionId, fileName_update, filePath_udpate, tableName_update, database, desc_update, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult = rdmService.editRefDataInfo(request_update);
        BaseRefTableVersionInfo editRefTableVersionInfo = updateResult.getBaseRefTableVersionInfo();
        RefTableVersionInfo refVersionInfo1 = rdmService.getRefVersionInfo(editRefTableVersionInfo.getVersionId());
        mockS3Object("test_update_s3.csv", "/test_update_s3.csv", refVersionInfo1);
        RefDataVersionFillInfo refDataVersionInfo = rdmService.fetchEditableRefDataVersionInfo(tableId);
        assertThat(refDataVersionInfo.getVersionId(), is(editRefTableVersionInfo.getVersionId()));
        assertThat(refDataVersionInfo.getTableId(), is(editRefTableVersionInfo.getTableId()));
        assertThat(refDataVersionInfo.getStartTime(), is(nullValue()));
        assertThat(refDataVersionInfo.getEndTime(), is(nullValue()));
        assertThat(refDataVersionInfo.getPublished(), is(false));
        assertThat(refDataVersionInfo.getShowStatus(), is(RefTableVersionStatus.UNPUBLISHED));
    }


    @Test
    public void test_fetch_ref_version_list() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        rdmService.publishRefDataTableVersion(versionId);
        //        update published verison
        String fileName_update = "test_update.csv";
        String tableName_update = "test_table_update";
        String filePath_udpate = "/test_update.csv";
        String desc_update = "test_desc_update";
        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId, versionId, fileName_update, filePath_udpate, tableName_update, database, desc_update, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult1 = rdmService.editRefDataInfo(request_update);
        BaseRefTableVersionInfo editRefTableVersionInfo = updateResult1.getBaseRefTableVersionInfo();
        RefTableVersionInfo refVersionInf_update = rdmService.getRefVersionInfo(editRefTableVersionInfo.getVersionId());
        rdmService.publishRefDataTableVersion(refVersionInf_update.getVersionId());
        String desc_update2 = "test_desc_update";
        EditRefDataTableRequest request_update2 = mockEditRefVersionInfo(tableId, refVersionInf_update.getVersionId(), fileName_update, filePath_udpate, tableName_update, database, desc_update2, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult2 = rdmService.editRefDataInfo(request_update2);
        List<BaseRefTableVersionInfo> list = rdmService.fetchRefVersionList(tableId);
        assertThat(list.size(), is(3));
        BaseRefTableVersionInfo v_edit = list.get(0);
        RefTableVersionInfo refVersionInf_update2 = rdmService.getRefVersionInfo(updateResult2.getBaseRefTableVersionInfo().getVersionId());
        assertThat(v_edit.getVersionId(), is(refVersionInf_update2.getVersionId()));
        assertThat(refVersionInf_update2.getStatus(), is(RefTableVersionStatus.UNPUBLISHED));
        assertThat(v_edit.getShowStatus(), is(refVersionInf_update2.getStatus()));
        assertThat(refVersionInf_update2.getPublished(), is(false));
        assertThat(v_edit.getPublished(), is(refVersionInf_update2.getPublished()));
        assertThat(refVersionInf_update2.getStartTime(), is(nullValue()));
        assertThat(v_edit.getStartTime(), is(refVersionInf_update2.getStartTime()));
        assertThat(refVersionInf_update2.getEndTime(), is(nullValue()));
        assertThat(v_edit.getEndTime(), is(refVersionInf_update2.getEndTime()));
        RefTableVersionInfo refVersionInf_update1 = rdmService.getRefVersionInfo(refVersionInf_update.getVersionId());
        BaseRefTableVersionInfo v_publish = list.get(1);
        assertThat(v_publish.getVersionId(), is(refVersionInf_update1.getVersionId()));
        assertThat(refVersionInf_update1.getStatus(), is(RefTableVersionStatus.PUBLISHED));
        assertThat(v_publish.getShowStatus(), is(refVersionInf_update1.getStatus()));
        assertThat(refVersionInf_update1.getPublished(), is(true));
        assertThat(v_publish.getPublished(), is(refVersionInf_update1.getPublished()));
        assertThat(refVersionInf_update1.getStartTime(), is(notNullValue()));
        assertThat(v_publish.getStartTime(), is(refVersionInf_update1.getStartTime()));
        assertThat(refVersionInf_update1.getEndTime(), is(nullValue()));
        assertThat(v_publish.getEndTime(), is(refVersionInf_update1.getEndTime()));
        BaseRefTableVersionInfo v_history = list.get(2);
        RefTableVersionInfo versionInfo_init = rdmService.getRefVersionInfo(versionId);
        assertThat(v_history.getVersionId(), is(versionInfo_init.getVersionId()));
        assertThat(versionInfo_init.getStatus(), is(RefTableVersionStatus.HISTORY));
        assertThat(v_history.getShowStatus(), is(versionInfo_init.getStatus()));
        assertThat(versionInfo_init.getPublished(), is(false));
        assertThat(v_history.getPublished(), is(versionInfo_init.getPublished()));
        assertThat(versionInfo_init.getStartTime(), is(notNullValue()));
        assertThat(v_history.getStartTime(), is(versionInfo_init.getStartTime()));
        assertThat(versionInfo_init.getEndTime(), is(notNullValue()));
        assertThat(v_history.getEndTime(), is(versionInfo_init.getEndTime()));


    }

    @Test
    public void test_page_ref_table_info() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        PageRequest pageRequest = new PageRequest(10, 1);
        PageResult<RefTableVersionFillInfo> page1 = rdmService.pageRefTableInfo(pageRequest);
        assertThat(page1.getTotalCount(), is(1));
        List<RefTableVersionFillInfo> records = page1.getRecords();
        assertThat(records.size(), is(1));
        RefTableVersionFillInfo refTableVersionFillInfo = records.get(0);
        assertThat(refTableVersionFillInfo.getVersionId(), is(versionId));
        assertThat(refTableVersionFillInfo.getTableId(), is(tableId));
        assertThat(refTableVersionFillInfo.getPublished(), is(false));
        assertThat(refTableVersionFillInfo.getShowStatus(), is(RefTableVersionStatus.UNPUBLISHED));
        rdmService.publishRefDataTableVersion(versionId);
        PageResult<RefTableVersionFillInfo> page2 = rdmService.pageRefTableInfo(pageRequest);
        assertThat(page2.getTotalCount(), is(1));
        List<RefTableVersionFillInfo> records2 = page2.getRecords();
        assertThat(records2.size(), is(1));
        RefTableVersionFillInfo refTableVersionFillInfo2 = records2.get(0);
        assertThat(refTableVersionFillInfo2.getVersionId(), is(versionId));
        assertThat(refTableVersionFillInfo2.getTableId(), is(tableId));
        assertThat(refTableVersionFillInfo2.getPublished(), is(true));
        assertThat(refTableVersionFillInfo2.getShowStatus(), is(RefTableVersionStatus.PUBLISHED));
    }

    @Test
    public void test_update_glossary() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        rdmService.publishRefDataTableVersion(versionId);
        Long datasetId = 1L;
        DatasetCreatedEvent event = new DatasetCreatedEvent(datasetId, datasourceId, database, table);
        mockUser("test");
        rdmService.linkage(event);
        rdmService.updateGlossary(1L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        List<Long> glossaryList1 = refVersionInfo.getGlossaryList();
        assertThat(glossaryList1.size(), is(glossaryList.size()));
        Assertions.assertTrue(glossaryList.containsAll(glossaryList1));

        rdmService.updateGlossary(3L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo1 = rdmService.getRefVersionInfo(versionId);
        List<Long> glossaryList2 = refVersionInfo1.getGlossaryList();
        assertThat(glossaryList2.size(), is(3));
        assertThat(glossaryList2, is(ImmutableList.of(1L, 2L, 3L)));

        rdmService.updateGlossary(1L, ImmutableList.of(2L));
        RefTableVersionInfo refVersionInfo3 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo3.getGlossaryList(), is(ImmutableList.of(2L, 3L)));
    }

    @Test
    public void test_update_glossary_edit_version() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        rdmService.publishRefDataTableVersion(versionId);
        Long datasetId = 1L;
        DatasetCreatedEvent event = new DatasetCreatedEvent(datasetId, datasourceId, database, table);
        mockUser("test");
        rdmService.linkage(event);

        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        String fileName = "test.csv";
        String filePath = "/test.csv";
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId, versionId, fileName, filePath, table, database, database, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult = rdmService.editRefDataInfo(request_update);
        Long update_version_id = updateResult.getBaseRefTableVersionInfo().getVersionId();
        RefTableVersionInfo refVersionInfo_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo_update.getGlossaryList(), is(glossaryList_update));

        rdmService.updateGlossary(1L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo1 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo1.getGlossaryList(), is(glossaryList));
        RefTableVersionInfo refVersionInfo1_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo1_update.getGlossaryList(), is(glossaryList_update));

        rdmService.updateGlossary(5L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo2 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo2.getGlossaryList(), is(ImmutableList.of(1L, 2L, 5L)));
        RefTableVersionInfo refVersionInfo2_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo2_update.getGlossaryList(), is(ImmutableList.of(1L, 2L, 3L, 4L, 5L)));

        rdmService.updateGlossary(1L, ImmutableList.of(2L));
        RefTableVersionInfo refVersionInfo3 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo3.getGlossaryList(), is(ImmutableList.of(2L, 5L)));
        RefTableVersionInfo refVersionInfo3_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo3_update.getGlossaryList(), is(ImmutableList.of(2L, 3L, 4L, 5L)));
    }

    @Test
    public void test_add_glossary() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        rdmService.publishRefDataTableVersion(versionId);
        Long datasetId = 1L;
        DatasetCreatedEvent event = new DatasetCreatedEvent(datasetId, datasourceId, database, table);
        mockUser("test");
        rdmService.linkage(event);

        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        String fileName = "test.csv";
        String filePath = "/test.csv";
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId, versionId, fileName, filePath, table, database, database, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult = rdmService.editRefDataInfo(request_update);
        Long update_version_id = updateResult.getBaseRefTableVersionInfo().getVersionId();
        RefTableVersionInfo refVersionInfo_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo_update.getGlossaryList(), is(glossaryList_update));

        rdmService.addGlossary(1L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo1 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo1.getGlossaryList(), is(glossaryList));
        RefTableVersionInfo refVersionInfo1_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo1_update.getGlossaryList(), is(glossaryList_update));

        rdmService.addGlossary(5L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo2 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo2.getGlossaryList(), is(ImmutableList.of(1L, 2L, 5L)));
        RefTableVersionInfo refVersionInfo2_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo2_update.getGlossaryList(), is(ImmutableList.of(1L, 2L, 3L, 4L, 5L)));

        rdmService.addGlossary(1L, ImmutableList.of(2L));
        RefTableVersionInfo refVersionInfo3 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo3.getGlossaryList(), is(ImmutableList.of(1L, 2L, 5L)));
        RefTableVersionInfo refVersionInfo3_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo3_update.getGlossaryList(), is(ImmutableList.of(1L, 2L, 3L, 4L, 5L)));
    }

    @Test
    public void test_remove_glossary_edit_version() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        rdmService.publishRefDataTableVersion(versionId);
        Long datasetId = 1L;
        DatasetCreatedEvent event = new DatasetCreatedEvent(datasetId, datasourceId, database, table);
        mockUser("user");
        rdmService.linkage(event);
        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        String fileName = "test.csv";
        String filePath = "/test.csv";
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId, versionId, fileName, filePath, table, database, database, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult = rdmService.editRefDataInfo(request_update);
        Long update_version_id = updateResult.getBaseRefTableVersionInfo().getVersionId();
        RefTableVersionInfo refVersionInfo_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo_update.getGlossaryList(), is(glossaryList_update));

        rdmService.removeGlossary(1L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo1 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo1.getGlossaryList(), is(ImmutableList.of(2L)));
        RefTableVersionInfo refVersionInfo1_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo1_update.getGlossaryList(), is(ImmutableList.of(2L, 3L, 4L)));

        rdmService.removeGlossary(5L, ImmutableList.of(datasetId));
        RefTableVersionInfo refVersionInfo2 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo2.getGlossaryList(), is(ImmutableList.of(2L)));
        RefTableVersionInfo refVersionInfo2_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo2_update.getGlossaryList(), is(ImmutableList.of(2L, 3L, 4L)));

        rdmService.removeGlossary(1L, ImmutableList.of(2L));
        RefTableVersionInfo refVersionInfo3 = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo3.getGlossaryList(), is(ImmutableList.of(2L)));
        RefTableVersionInfo refVersionInfo3_update = rdmService.getRefVersionInfo(update_version_id);
        assertThat(refVersionInfo3_update.getGlossaryList(), is(ImmutableList.of(2L, 3L, 4L)));
    }

    private void mockUser(String user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(user);
//        RequestResult<UserInfo> requestResult = RequestResult.success(userInfo);
//        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.GET), any(), Mockito.<ParameterizedTypeReference<RequestResult<UserInfo>>>any()))
//                .thenReturn(new ResponseEntity<>(requestResult, HttpStatus.OK));
        doReturn(userInfo).when(rdmService).getUserByUsername(anyString());
    }

    @Test
    public void test_publish_linkage() {
        List<Long> glossaryList = ImmutableList.of(1L, 2L);
        List<String> ownerList = ImmutableList.of("user1", "user2");
        LinkedHashMap<ConstraintType, Set<String>> map = Maps.newLinkedHashMap();
        String table = "test_table";
        String database = "test_database";
        RefUpdateResult refUpdateResult = init_create_ref_table_version(table, database, glossaryList, ownerList, map);
        BaseRefTableVersionInfo versionInfo = refUpdateResult.getBaseRefTableVersionInfo();
        Long tableId = versionInfo.getTableId();
        Long versionId = versionInfo.getVersionId();
        rdmService.publishRefDataTableVersion(versionId);
        Long datasetId = 1L;
        DatasetCreatedEvent event = new DatasetCreatedEvent(datasetId, datasourceId, database, table);
        mockUser("test");
        rdmService.linkage(event);
        RefTableVersionInfo refVersionInfo = rdmService.getRefVersionInfo(versionId);
        assertThat(refVersionInfo.getDatasetId(), is(datasetId));

        List<Long> glossaryList_update = ImmutableList.of(1L, 2L, 3L, 4L);
        List<String> ownerList_update = ImmutableList.of("user1");
        String fileName = "test.csv";
        String filePath = "/test.csv";
        EditRefDataTableRequest request_update = mockEditRefVersionInfo(tableId, versionId, fileName, filePath, table, database, database, glossaryList_update, ownerList_update, map);
        RefUpdateResult updateResult = rdmService.editRefDataInfo(request_update);
        Long update_version_id = updateResult.getBaseRefTableVersionInfo().getVersionId();
        RefTableVersionInfo refVersionInfo_update = rdmService.getRefVersionInfo(update_version_id);

        assertThat(refVersionInfo_update.getDatasetId(), is(datasetId));
    }


    private RefUpdateResult init_create_ref_table_version(String tableName, String database, List<Long> glossaryList, List<String> ownerList, LinkedHashMap<ConstraintType, Set<String>> map) {
        Long tableId = null;
        Long versionId = null;
        String fileName = "test.csv";
        String filePath = "/test.csv";
        String desc = "test_desc";
        EditRefDataTableRequest request = mockEditRefVersionInfo(tableId, versionId, fileName, filePath, tableName, database, desc, glossaryList, ownerList, map);
        return rdmService.editRefDataInfo(request);
    }

    private EditRefDataTableRequest mockEditRefVersionInfo(Long tableId, Long versionId, String fileName, String filePath, String tableName, String database, String desc,
                                                           List<Long> glossaryList, List<String> ownerList, LinkedHashMap<ConstraintType, Set<String>> map) {
        RefBaseTable refBaseTable = rdmService.parseFile(getMultipartCSVFile(fileName, filePath));
        RefTableMetaData refTableMetaData = refBaseTable.getRefTableMetaData();
        refTableMetaData.setRefTableConstraints(map);
        refBaseTable.setRefTableMetaData(refTableMetaData);
        EditRefTableVersionInfo editRefVersionInfo = new EditRefTableVersionInfo(tableName, database, desc, glossaryList, ownerList, refTableMetaData);
        EditRefDataTableRequest request = new EditRefDataTableRequest();
        request.setRefBaseTable(refBaseTable);
        request.setTableId(tableId);
        request.setVersionId(versionId);
        request.setEditRefTableVersionInfo(editRefVersionInfo);
        return request;
    }


}