package com.miotech.kun.datadiscovery.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.common.model.PageRequest;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.EditRefDataTableRequest;
import com.miotech.kun.datadiscovery.model.bo.EditRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.bo.ValidRefDataRequest;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfo;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.model.enums.RefTableVersionStatus;
import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.vo.RefDataVersionFillInfo;
import com.miotech.kun.datadiscovery.model.vo.RefTableVersionFillInfo;
import com.miotech.kun.datadiscovery.model.vo.ValidationResultVo;
import com.miotech.kun.datadiscovery.persistence.RefTableVersionRepository;
import com.miotech.kun.datadiscovery.service.rdm.RefDataOperator;
import com.miotech.kun.datadiscovery.service.rdm.RefDataValidator;
import com.miotech.kun.datadiscovery.service.rdm.file.RefStorageFileBuilder;
import com.miotech.kun.datadiscovery.service.rdm.file.RefUploadFileBuilder;
import com.miotech.kun.datadiscovery.service.rdm.file.S3StoragePathGenerator;
import com.miotech.kun.metadata.core.model.event.DatasetCreatedEvent;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.datadiscovery.constant.Constants.INPUT_MAX_LINE;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-20 17:30
 **/
@RequiredArgsConstructor
@Service
@Slf4j
public class RdmService extends BaseSecurityService {

    private final RefUploadFileBuilder refUploadFileBuilder;
    private final RefStorageFileBuilder refStorageFileBuilder;
    private final RefDataValidator refDataValidator;
    private final RefDataOperator refDataOperator;
    private final GlossaryService glossaryService;
    private final LineageAppService lineageAppService;
    private final FilterRuleAppService filterRuleAppService;
    private final RefTableVersionRepository refTableVersionRepository;

    @Value("${rdm.datasource:0}")
    private Long datasourceId;

    @Transactional(rollbackFor = Exception.class)
    public RefBaseTable parseFile(MultipartFile file) {
        log.debug("file info:name:{},getOriginalFilename:{}", file.getName(), file.getOriginalFilename());
        RefInputSource refInputSource;
        try {
            refInputSource = new RefInputSource(file);
        } catch (IOException e) {
            log.error("file upload is error", e);
            throw new IllegalStateException("file upload is error", e);
        }

        RefBaseTable refBaseTable = null;
        try {
            refBaseTable = refUploadFileBuilder.parse(refInputSource);
        } catch (IOException e) {
            log.error("file parse is error", e);
            throw new IllegalStateException("file parse is error", e);
        } finally {
            refInputSource.close();
        }
        List<DataRecord> data = refBaseTable.getRefData().getData();
        if (data.size() < 1) {
            throw new IllegalArgumentException(String.format("The number of rows cannot be less than 1:%s", data.size()));
        }
        if (data.size() > INPUT_MAX_LINE) {
            throw new IllegalArgumentException(String.format("he number of rows cannot be greater than %s ,size:%s", INPUT_MAX_LINE, data.size()));
        }
        return refBaseTable;
    }

    public ValidationResultVo validRefDataInfo(ValidRefDataRequest request) {
        ValidationResult validationResult = refDataValidator.valid(request.getRefBaseTable());
        return new ValidationResultVo(validationResult);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.RDM_EDIT_VERSION_INFO, args = {"#request.tableId", "#request.versionId", "#request.editRefTableVersionInfo"})
    public RefUpdateResult editRefDataInfo(EditRefDataTableRequest request) {
        EditRefTableVersionInfo editRefVersionInfo = request.getEditRefTableVersionInfo();
        RefBaseTable refBaseTable = request.getRefBaseTable();
        String tableName = editRefVersionInfo.getTableName();
        Preconditions.checkArgument(RefDataValidator.validateName(tableName), "table name format error:%s", tableName);
        ValidationResult validationResult = refDataValidator.valid(refBaseTable);
        if (!validationResult.getStatus()) {
            return RefUpdateResult.error(new ValidationResultVo(validationResult));
        }
        editRefVersionInfo.setRefTableMetaData(refBaseTable.getRefTableMetaData());
        RefTableVersionInfo version = null;
        Long tableId = request.getTableId();
        Long versionId = request.getVersionId();
        if (Objects.nonNull(versionId) && Objects.nonNull(tableId)) {
            version = saveOrUpdateRefDataVersionInfo(versionId, editRefVersionInfo);
        } else {
            version = initRefDataVersion(editRefVersionInfo);
        }
        try {
            refStorageFileBuilder.overwrite(new StorageFileData(version.getDataPath(), version.getSchemaName(), refBaseTable));
        } catch (IOException e) {
            log.error("Data writing failed ,edit base info:{}", editRefVersionInfo);
            throw new IllegalStateException("Data writing failed", e);
        }

        return RefUpdateResult.success(new BaseRefTableVersionInfo(version));

    }


    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.RDM_PUBLISH, args = {"#versionId"})
    public boolean publishRefDataTableVersion(Long versionId) {
        RefTableVersionInfo refTableVersionInfo = getRefVersionInfo(versionId);
        disposeGlossary(refTableVersionInfo.getGlossaryList(), refTableVersionInfo.getDatasetId());
        RefTableVersionInfo versionInfo = publishRefDataTableVersion(refTableVersionInfo);
        refDataOperator.overwrite(versionInfo);
        return true;
    }

    private void disposeGlossary(List<Long> updateGlossaryIdList, Long datasetId) {
        if (Objects.isNull(datasetId)) {
            log.debug("dataset id is null");
            return;
        }
        List<Long> currentList = glossaryService.getGlossariesByDataset(datasetId).stream().map(GlossaryBasicInfo::getId).collect(Collectors.toList());
        List<Long> glossaryList = glossaryService.findGlossaryList(updateGlossaryIdList).stream().map(GlossaryBasicInfo::getId).collect(Collectors.toList());
        Collection<Long> intersection = CollectionUtils.intersection(glossaryList, currentList);
        Collection<Long> subtractRemove = CollectionUtils.subtract(currentList, intersection);
        subtractRemove.forEach(id -> glossaryService.removeGlossaryResource(id, ImmutableList.of(datasetId)));
        Collection<Long> subtractAdd = CollectionUtils.subtract(glossaryList, intersection);
        subtractAdd.forEach(id -> glossaryService.addGlossaryResource(id, ImmutableList.of(datasetId)));
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.RDM_EDIT_ADD_GLOSSARY, args = {"#glossaryId", "#datasetIds"})
    public void addGlossary(Long glossaryId, Collection<Long> datasetIds) {
        if (CollectionUtils.isEmpty(datasetIds)) {
            return;
        }
        OffsetDateTime now = DateTimeUtils.now();
        List<RefTableVersionInfo> editGlossaryTableList = refTableVersionRepository.findEditGlossaryTableList();
        editGlossaryTableList.stream()
                .filter(refTableVersionInfo -> datasetIds.contains(refTableVersionInfo.getDatasetId()))
                .filter(refTableVersionInfo -> (CollectionUtils.isEmpty(refTableVersionInfo.getGlossaryList())) || (!refTableVersionInfo.getGlossaryList().contains(glossaryId)))
                .forEach(refTableVersionInfo -> {
                    if (Objects.isNull(refTableVersionInfo.getGlossaryList())) {
                        refTableVersionInfo.setGlossaryList(Lists.newArrayList(glossaryId));
                    } else {
                        refTableVersionInfo.getGlossaryList().add(glossaryId);
                    }
                    refTableVersionInfo.setUpdateUser(getCurrentUsername());
                    refTableVersionInfo.setUpdateTime(now);
                    refTableVersionRepository.update(refTableVersionInfo);
                });


    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.RDM_EDIT_REMOVE_GLOSSARY, args = {"#glossaryId", "#datasetIds"})
    public void removeGlossary(Long glossaryId, Collection<Long> datasetIds) {
        if (CollectionUtils.isEmpty(datasetIds)) {
            return;
        }
        OffsetDateTime now = DateTimeUtils.now();
        List<RefTableVersionInfo> editGlossaryTableList = refTableVersionRepository.findEditGlossaryTableList();
        editGlossaryTableList.stream()
                .filter(refTableVersionInfo -> datasetIds.contains(refTableVersionInfo.getDatasetId()))
                .filter(refTableVersionInfo -> (CollectionUtils.isNotEmpty(refTableVersionInfo.getGlossaryList())) && (refTableVersionInfo.getGlossaryList().contains(glossaryId)))
                .forEach(refTableVersionInfo -> {
                    refTableVersionInfo.getGlossaryList().remove(glossaryId);
                    refTableVersionInfo.setUpdateUser(getCurrentUsername());
                    refTableVersionInfo.setUpdateTime(now);
                    refTableVersionRepository.update(refTableVersionInfo);
                });
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.RDM_ROLLBACK, args = {"#versionId"})
    public boolean rollbackRefDataTableVersion(Long versionId) {
        RefTableVersionInfo refTableVersionInfo = getRefVersionInfo(versionId);
        disposeGlossary(refTableVersionInfo.getGlossaryList(), refTableVersionInfo.getDatasetId());
        RefTableVersionInfo versionInfo = rollbackRefDataTableVersion(refTableVersionInfo);
        refDataOperator.overwrite(versionInfo);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.RDM_EDIT_DEACTIVATE, args = {"#versionId"})
    public boolean deactivateRefDataTableVersion(Long versionId) {
        RefTableVersionInfo refVersionInfo = getRefVersionInfo(versionId);
        List<DatasetNodeInfo> downstreamDataset = lineageAppService.getDownstreamDataset(refVersionInfo.getDatasetId());
        if (CollectionUtils.isEmpty(downstreamDataset)) {
            deactivateTableVersion(refVersionInfo);
            refDataOperator.remove(refVersionInfo.getDatabaseName(), refVersionInfo.getTableName());
            filterRuleAppService.removeDatasetStatistics(refVersionInfo.getDatasetId());
            return true;
        }
        String datasets = downstreamDataset.stream().map(DatasetNodeInfo::getDatasetName).collect(Collectors.joining(","));
        log.debug("lineage info exists:{}", datasets);
        throw new IllegalStateException(String.format("lineage info exists:%s", datasets));
    }


    public RefDataVersionFillInfo fetchRefDataVersionInfo(Long versionId) {
        RefTableVersionInfo refTableVersionInfo = getRefVersionInfo(versionId);
        return getRefDataVersionFillInfo(refTableVersionInfo);
    }

    public RefDataVersionFillInfo fetchEditableRefDataVersionInfo(Long tableId) {
        RefTableVersionInfo refTableVersionInfo = refTableVersionRepository.findUnPublishedByTable(tableId);
        if (Objects.isNull(refTableVersionInfo)) {
            refTableVersionInfo = refTableVersionRepository.findPublishByTableId(tableId);
        }
        if (Objects.isNull(refTableVersionInfo)) {
            log.error("Table unavailable tableId:{}", tableId);
            throw new IllegalStateException(String.format("Table unavailable tableId:%s", tableId));
        }
        return getRefDataVersionFillInfo(refTableVersionInfo);
    }

    public List<BaseRefTableVersionInfo> fetchRefVersionList(Long tableId) {
        return refTableVersionRepository.findByTableId(tableId)
                .stream()
                .map(BaseRefTableVersionInfo::new)
                .sorted(Comparator.comparing(BaseRefTableVersionInfo::showSorted).thenComparing(Comparator.comparing(BaseRefTableVersionInfo::getUpdateTime).reversed()))
                .collect(Collectors.toList());
    }


    public PageResult<RefTableVersionFillInfo> pageRefTableInfo(PageRequest pageRequest) {
        PageResult<RefTableVersionInfo> pageResult = refTableVersionRepository.pageRefTableInfo(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<RefTableVersionFillInfo> refTableVersionFillInfoList = pageResult.getRecords().stream().map(refTableVersionInfo -> {
            RefTableVersionFillInfo refTableVersionFillInfo = new RefTableVersionFillInfo(refTableVersionInfo);
            fillRefTableVersionFillInfo(refTableVersionInfo, refTableVersionFillInfo);
            return refTableVersionFillInfo;
        }).collect(Collectors.toList());
        return new PageResult<>(pageResult.getPageSize(), pageResult.getPageNumber(), pageResult.getTotalCount(), refTableVersionFillInfoList);
    }

    private void fillRefTableVersionFillInfo(RefTableVersionInfo refTableVersionInfo, RefTableVersionFillInfo refTableVersionFillInfo) {
        refTableVersionFillInfo.setGlossaryList(glossaryService.findGlossaryList(refTableVersionInfo.getGlossaryList()));
        Long datasetId = refTableVersionInfo.getDatasetId();
        if (refTableVersionInfo.getPublished() && Objects.nonNull(datasetId)) {
            List<Pair<Long, String>> lineageList = lineageAppService.getDownstreamDataset(datasetId)
                    .stream()
                    .map(datasetNodeInfo -> new ImmutablePair<>(datasetNodeInfo.getGid(), datasetNodeInfo.getDatasetName()))
                    .collect(Collectors.toList());
            refTableVersionFillInfo.setLineageDatasetList(lineageList);
        }

    }

    private RefDataVersionFillInfo getRefDataVersionFillInfo(RefTableVersionInfo refTableVersionInfo) {
        RefDataVersionFillInfo refDataVersionFillInfo = new RefDataVersionFillInfo(refTableVersionInfo);
        refDataVersionFillInfo.setEnableEditName(!refTableVersionRepository.hasPublished(refTableVersionInfo.getTableId()));
        refDataVersionFillInfo.setEdit(Objects.isNull(refTableVersionInfo.getEndTime()));
        StorageFileData storageFileData;
        try {
            storageFileData = refStorageFileBuilder.read(refTableVersionInfo);
        } catch (IOException e) {
            log.error(" storage data read error,table name:{},version number:V{}", refTableVersionInfo.getTableName(), refTableVersionInfo.getVersionNumber(), e);
            throw new DataAccessResourceFailureException(String.format(" storage data read error,table name:%s,version number:V%s", refTableVersionInfo.getTableName(), refTableVersionInfo.getVersionNumber()), e);
        }
        refDataVersionFillInfo.setRefBaseTable(storageFileData.getRefBaseTable());
        fillRefTableVersionFillInfo(refTableVersionInfo, refDataVersionFillInfo);
        return refDataVersionFillInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.RDM_LINKAGE, args = {"#event"})
    public void linkage(DatasetCreatedEvent event) {
        if (Objects.isNull(event.getDataSourceId()) || (!event.getDataSourceId().equals(datasourceId))) {
            return;
        }
        String database = event.getDatabase();
        String table = event.getTable();
        if (!refTableVersionRepository.existsDatabaseTableInfo(database, table, null)) {
            log.debug("table does not exist,database:{},table:{}", database, table);
            return;
        }
        refTableVersionRepository.updateDataSetId(database, table, event.getDatasetId());
        RefTableVersionInfo refTableVersionInfo = refTableVersionRepository.findPublishedByDatasetId(event.getDatasetId());
        if (Objects.isNull(refTableVersionInfo)) {
            log.debug("published table does not exist,database:{},table:{}", database, table);
            return;
        }
        filterRuleAppService.addDatasetStatistics(event.getDatasetId());
        List<Long> glossaryList = refTableVersionInfo.getGlossaryList();
        if (CollectionUtils.isEmpty(glossaryList)) {
            log.debug("glossaryList is empty:{}", event);
            return;
        }
        try {
            String user = refTableVersionInfo.getCreateUser();
            setCurrentUser(getUserByUsername(user));
            disposeGlossary(glossaryList, event.getDatasetId());
        } catch (Exception e) {
            log.error("glossary update error:{},glossary:{}", event, glossaryList, e);
        }
    }


    private RefTableVersionInfo saveOrUpdateRefDataVersionInfo(Long versionId, EditRefTableVersionInfo editInfo) {
        RefTableVersionInfo refVersionInfo = getRefVersionInfo(versionId);
        RefTableVersionStatus status = refVersionInfo.getStatus();
        if (RefTableVersionStatus.PUBLISHED.equals(status)) {
//            基于发布的版本保存
            return createNextRefDataVersion(refVersionInfo, editInfo);
        }
        if (!RefTableVersionStatus.UNPUBLISHED.equals(status)) {
            Integer versionNumber = refVersionInfo.getVersionNumber();
            log.error("This version cannot be updated,version id:{},version number:{}", versionId, versionNumber);
            throw new IllegalStateException(String.format("This version cannot be update,version id:%s,version number:%s", versionId, versionNumber));
        }

//        更新未发布的版本
        String databaseName = editInfo.getDatabaseName();
        String tableName = editInfo.getTableName();
        existsDatabaseTableInfo(databaseName, tableName, refVersionInfo.getTableId());
        OffsetDateTime now = DateTimeUtils.now();
        refVersionInfo.setVersionDescription(editInfo.getVersionDescription());
        refVersionInfo.setTableName(editInfo.getTableName());
        refVersionInfo.setDatabaseName(editInfo.getDatabaseName());
        refVersionInfo.setGlossaryList(editInfo.getGlossaryList());
        refVersionInfo.setOwnerList(getOwnerList(editInfo));
        refVersionInfo.setRefTableColumns(editInfo.getRefTableMetaData().getColumns());
        refVersionInfo.setRefTableConstraints(editInfo.getRefTableMetaData().getRefTableConstraints());
        refVersionInfo.setUpdateUser(getCurrentUsername());
        refVersionInfo.setUpdateTime(now);
        return refTableVersionRepository.update(refVersionInfo);
    }

    private List<String> getOwnerList(EditRefTableVersionInfo editInfo) {
        if (CollectionUtils.isEmpty(editInfo.getOwnerList())) {
            return Lists.newArrayList(getCurrentUsername());
        }
        return editInfo.getOwnerList();
    }

    private RefTableVersionInfo createNextRefDataVersion(RefTableVersionInfo publishedRefTableVersionInfo, EditRefTableVersionInfo editInfo) {
        Preconditions.checkNotNull(publishedRefTableVersionInfo, "RefTableVersionInfo is not null");
        Preconditions.checkNotNull(editInfo, "RefTableVersionInfo is not null");
        Long versionId = publishedRefTableVersionInfo.getVersionId();
        Integer versionNumber = publishedRefTableVersionInfo.getVersionNumber();
        if (!publishedRefTableVersionInfo.getPublished()) {
            log.error("Cannot rely on unpublished versions to create,version id:{},version number:{}", versionId, versionNumber);
            throw new IllegalStateException(String.format("Cannot rely on unpublished versions to create,version id:%s,version number:%s", versionId, versionNumber));
        }
        RefTableVersionInfo unPublishedVersionInfo = refTableVersionRepository.findUnPublishedByTable(publishedRefTableVersionInfo.getTableId());
        if (Objects.nonNull(unPublishedVersionInfo)) {
            log.error("already an unpublished version,version id:{},version number:{}", unPublishedVersionInfo.getVersionId(), unPublishedVersionInfo.getVersionNumber());
            throw new IllegalStateException(String.format(" already an unpublished version,,version id:%s,version number:%s", unPublishedVersionInfo.getVersionId(), unPublishedVersionInfo.getVersionNumber()));
        }
        LinkedHashSet<RefColumn> columns = editInfo.getRefTableMetaData().getColumns();
        LinkedHashSet<RefColumn> refTableColumns = publishedRefTableVersionInfo.getRefTableColumns();
        if (!columns.containsAll(refTableColumns)) {
            log.error("The column information of the published table cannot be updated, and only new columns can be added,version id:{},version number:{}", versionId, versionNumber);
            throw new IllegalStateException(String.format(" The column information of the published table cannot be updated, and only new columns can be added,version id:%s,version number:%s", versionId, versionNumber));

        }

        RefTableVersionInfo refTableVersionInfo = new RefTableVersionInfo();
        refTableVersionInfo.setVersionId(IdGenerator.getInstance().nextId());
        refTableVersionInfo.setTableId(publishedRefTableVersionInfo.getTableId());
        refTableVersionInfo.setTableName(publishedRefTableVersionInfo.getTableName());
        refTableVersionInfo.setDatabaseName(publishedRefTableVersionInfo.getDatabaseName());
        refTableVersionInfo.setDatasetId(publishedRefTableVersionInfo.getDatasetId());
        refTableVersionInfo.setVersionDescription(editInfo.getVersionDescription());
        refTableVersionInfo.setGlossaryList(editInfo.getGlossaryList());
        refTableVersionInfo.setOwnerList(getOwnerList(editInfo));
        refTableVersionInfo.setRefTableColumns(columns);
        refTableVersionInfo.setRefTableConstraints(editInfo.getRefTableMetaData().getRefTableConstraints());
        OffsetDateTime now = DateTimeUtils.now();
        refTableVersionInfo.setVersionNumber(null);
        refTableVersionInfo.setPublished(false);
        refTableVersionInfo.setStatus(RefTableVersionStatus.UNPUBLISHED);
        refTableVersionInfo.setCreateUser(getCurrentUsername());
        refTableVersionInfo.setCreateTime(now);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionInfo.setDataPath(S3StoragePathGenerator.getVersionDataPath(refTableVersionInfo));
        return refTableVersionRepository.create(refTableVersionInfo);
    }

    private Integer getMaxVersionNumberByTable(Long tableId) {
        return refTableVersionRepository.findMaxVersionNumberByTable(tableId);
    }

    private RefTableVersionInfo initRefDataVersion(EditRefTableVersionInfo editRefVersionInfo) {
        String databaseName = editRefVersionInfo.getDatabaseName();
        String tableName = editRefVersionInfo.getTableName();
        existsDatabaseTableInfo(databaseName, tableName, null);
        RefTableVersionInfo refTableVersionInfo = new RefTableVersionInfo();
        refTableVersionInfo.setVersionId(IdGenerator.getInstance().nextId());
        refTableVersionInfo.setTableId(IdGenerator.getInstance().nextId());
        refTableVersionInfo.setTableName(tableName);
        refTableVersionInfo.setDatabaseName(databaseName);
        refTableVersionInfo.setVersionDescription(editRefVersionInfo.getVersionDescription());
        refTableVersionInfo.setGlossaryList(editRefVersionInfo.getGlossaryList());
        refTableVersionInfo.setOwnerList(getOwnerList(editRefVersionInfo));
        refTableVersionInfo.setRefTableColumns(editRefVersionInfo.getRefTableMetaData().getColumns());
        refTableVersionInfo.setRefTableConstraints(editRefVersionInfo.getRefTableMetaData().getRefTableConstraints());
        OffsetDateTime now = DateTimeUtils.now();
        refTableVersionInfo.setVersionNumber(null);
        refTableVersionInfo.setPublished(false);
        refTableVersionInfo.setStatus(RefTableVersionStatus.UNPUBLISHED);
        refTableVersionInfo.setCreateUser(getCurrentUsername());
        refTableVersionInfo.setCreateTime(now);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionInfo.setDataPath(S3StoragePathGenerator.getVersionDataPath(refTableVersionInfo));
        return refTableVersionRepository.create(refTableVersionInfo);
    }


    private RefTableVersionInfo publishRefDataTableVersion(RefTableVersionInfo refTableVersionInfo) {
        Preconditions.checkNotNull(refTableVersionInfo, "param:refTableVersionInfo  is not null");
        if (refTableVersionInfo.getPublished()) {
            log.warn("version is published,version number:{}", refTableVersionInfo.getVersionNumber());
            throw new IllegalStateException(String.format("version is published,version number:V%s", refTableVersionInfo.getVersionNumber()));
        }
        if (Objects.nonNull(refTableVersionInfo.getEndTime())) {
            log.warn("Historical versions cannot be published directly,version number:{}", refTableVersionInfo.getVersionNumber());
            throw new IllegalStateException(String.format("Historical versions cannot be published directly，version number:V%s", refTableVersionInfo.getVersionNumber()));
        }
        Long tableId = refTableVersionInfo.getTableId();
        existsDatabaseTableInfo(refTableVersionInfo.getDatabaseName(), refTableVersionInfo.getTableName(), tableId);
        RefTableVersionInfo latestRefVersionInfo = refTableVersionRepository.findPublishByTableId(tableId);
        if (Objects.nonNull(latestRefVersionInfo)) {
            deactivateTableVersion(latestRefVersionInfo);
        }
        OffsetDateTime now = DateTimeUtils.now();
        Integer maxVersionNumberByTable = getMaxVersionNumberByTable(tableId);
        if (Objects.isNull(maxVersionNumberByTable)) {
            maxVersionNumberByTable = 0;
        }
        refTableVersionInfo.setVersionNumber(maxVersionNumberByTable + 1);
        refTableVersionInfo.setPublished(true);
        refTableVersionInfo.setStatus(RefTableVersionStatus.PUBLISHED);
        refTableVersionInfo.setStartTime(now);
        refTableVersionInfo.setEndTime(null);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionRepository.update(refTableVersionInfo);
        return refTableVersionInfo;
    }

    public RefTableVersionInfo getRefVersionInfo(Long versionId) {
        return refTableVersionRepository.findByVersionId(versionId);
    }

    private void deactivateTableVersion(RefTableVersionInfo refTableVersionInfo) {
        Preconditions.checkNotNull(refTableVersionInfo, "version info is not null");
        Preconditions.checkNotNull(refTableVersionInfo.getVersionId(), "version id is not null");
        if (!refTableVersionInfo.getPublished()) {
            log.warn("version is unpublished,version number:{}", refTableVersionInfo.getVersionNumber());
            throw new IllegalStateException(String.format("version is unpublished,version number:%s", refTableVersionInfo.getVersionNumber()));
        }
        OffsetDateTime now = DateTimeUtils.now();
        refTableVersionInfo.setPublished(false);
        refTableVersionInfo.setStatus(RefTableVersionStatus.HISTORY);
        refTableVersionInfo.setEndTime(now);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionRepository.update(refTableVersionInfo);
    }

    private RefTableVersionInfo rollbackRefDataTableVersion(RefTableVersionInfo refTableVersionInfo) {
        Preconditions.checkNotNull(refTableVersionInfo, "param:refTableVersionInfo  is not null");
        if (Objects.isNull(refTableVersionInfo.getEndTime())) {
            Integer versionNumber = refTableVersionInfo.getVersionNumber();
            log.warn("Rollback is not supported for non historical versions,version number:{}", versionNumber);
            throw new IllegalStateException(String.format("Rollback is not supported for non historical versions，version number:V%s", Objects.isNull(versionNumber) ? "-" : versionNumber));
        }
        RefTableVersionInfo latestRefVersionInfo = refTableVersionRepository.findPublishByTableId(refTableVersionInfo.getTableId());
        if (Objects.nonNull(latestRefVersionInfo)) {
            log.warn("There is already a published version. Please deactivate the published version before rollback,version number:{}", latestRefVersionInfo.getVersionNumber());
            throw new IllegalStateException(String.format("There is already a published version. Please deactivate the published version before rollback,version number:V%s", latestRefVersionInfo.getVersionNumber()));

        }
        OffsetDateTime now = DateTimeUtils.now();
        refTableVersionInfo.setPublished(true);
        refTableVersionInfo.setStatus(RefTableVersionStatus.PUBLISHED);
        refTableVersionInfo.setStartTime(now);
        refTableVersionInfo.setEndTime(null);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionRepository.update(refTableVersionInfo);
        return refTableVersionInfo;
    }

    private void existsDatabaseTableInfo(String databaseName, String tableName, Long notTableId) {
        boolean existsDatabaseTableInfo = refTableVersionRepository.existsDatabaseTableInfo(databaseName, tableName, notTableId);
        if (existsDatabaseTableInfo) {
            log.error("Table already exists,database name:{},table name:{}", databaseName, tableName);
            throw new IllegalStateException(String.format("Table already exists,database name:%s,table name:%s", databaseName, tableName));
        }
    }

}
