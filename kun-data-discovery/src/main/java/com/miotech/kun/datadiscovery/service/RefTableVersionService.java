package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.persistence.RefTableVersionRepository;
import com.miotech.kun.datadiscovery.service.rdm.file.S3StorageFileManger;
import com.miotech.kun.security.service.BaseSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-20 23:46
 **/
@RequiredArgsConstructor
@Service
@Slf4j
public class RefTableVersionService extends BaseSecurityService {
    private final RefTableVersionRepository refTableVersionRepository;

    @Value("${rdm.database-name:default}")
    private String databaseName;

    @Transactional(rollbackFor = Exception.class)
    public synchronized RefTableVersionInfo updateRefDataVersion(Long tableId, Long versionId, RefTableVersionInfo editInfo) {
        RefTableVersionInfo maxRefVersionInfo = getMaxRefVersionInfoByTable(tableId);
        if (Objects.isNull(maxRefVersionInfo)) {
            log.error(" Version does not exist version tableId{}", tableId);
            throw new IllegalStateException(String.format("Version does not exist table id:%s", tableId));
        }
        if (maxRefVersionInfo.getPublished()) {
            log.error("Table has been published table:{},version:{}", tableId, maxRefVersionInfo.getVersionId());
            throw new IllegalStateException(String.format("Table has been published table:%s,version:%s", tableId, maxRefVersionInfo.getVersionId()));
        }

        if (!Objects.equals(maxRefVersionInfo.getVersionId(), versionId)) {
            log.error(" Version is not support edit version id:{}", versionId);
            throw new IllegalStateException(String.format("Version is not support edit version id:{}:%s", versionId));
        }
        OffsetDateTime now = DateTimeUtils.now();
        maxRefVersionInfo.setVersionDescription(editInfo.getVersionDescription());
        maxRefVersionInfo.setTableName(editInfo.getTableName());
        maxRefVersionInfo.setTableDsi(createDsi(editInfo.getTableName()));
        maxRefVersionInfo.setGlossaryList(editInfo.getGlossaryList());
        maxRefVersionInfo.setOwnerList(editInfo.getOwnerList());
        maxRefVersionInfo.setRefTableColumns(editInfo.getRefTableColumns());
        maxRefVersionInfo.setRefTableConstraints(editInfo.getRefTableConstraints());
        maxRefVersionInfo.setUpdateUser(getCurrentUsername());
        maxRefVersionInfo.setUpdateTime(now);
        return refTableVersionRepository.update(maxRefVersionInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefTableVersionInfo createInitRefDataVersion(RefTableVersionInfo refTableVersionInfo) {
        refTableVersionInfo.setVersionId(IdGenerator.getInstance().nextId());
        refTableVersionInfo.setTableId(IdGenerator.getInstance().nextId());
        OffsetDateTime now = DateTimeUtils.now();
        refTableVersionInfo.setVersionNumber(1);
        refTableVersionInfo.setPublished(false);
        refTableVersionInfo.setCreateUser(getCurrentUsername());
        refTableVersionInfo.setCreateTime(now);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionInfo.setDataPath(S3StorageFileManger.concatDataPath(refTableVersionInfo.getTableName(), refTableVersionInfo.getVersionNumber()));
        refTableVersionInfo.setTableDsi(createDsi(refTableVersionInfo.getTableName()));
        return refTableVersionRepository.create(refTableVersionInfo);
    }

    private String createDsi(String tableName) {
        return new StringJoiner(":")
                .add(databaseName)
                .add(tableName).toString();
    }


    @Transactional(rollbackFor = Exception.class)
    public RefTableVersionInfo activateRefDataTableVersion(Long versionId) {
        RefTableVersionInfo refTableVersionInfo = getRefVersionInfo(versionId);
        if (refTableVersionInfo.getPublished()) {
            log.warn("version is published,version number:{}", refTableVersionInfo.getVersionNumber());
            throw new IllegalStateException(String.format("version is published,version number:%s", refTableVersionInfo.getVersionNumber()));
        }
        RefTableVersionInfo latestRefVersionInfo = getPublishRefVersionInfoByTable(refTableVersionInfo.getTableId());
        if (Objects.nonNull(latestRefVersionInfo)) {
            Long latestRefVersionInfoVersionId = latestRefVersionInfo.getVersionId();
            Integer versionNumber = latestRefVersionInfo.getVersionNumber();
            log.warn("You need to deactivate the released version id:{},version number:{}", latestRefVersionInfoVersionId, versionNumber);
            throw new IllegalStateException(String.format("You need to deactivate the released version id:%s,version number:%s", latestRefVersionInfoVersionId, versionNumber));
        }
        OffsetDateTime now = DateTimeUtils.now();
        refTableVersionInfo.setPublished(true);
        refTableVersionInfo.setStartTime(now);
        refTableVersionInfo.setEndTime(null);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionRepository.update(refTableVersionInfo);
        return refTableVersionInfo;
    }

    public RefTableVersionInfo getPublishRefVersionInfoByTable(Long tableId) {
        return refTableVersionRepository.selectPublishRefVersionInfoByTable(tableId);
    }

    public RefTableVersionInfo getMaxRefVersionInfoByTable(Long tableId) {
        return refTableVersionRepository.selectMaxRefVersionInfoByTable(tableId);
    }

    public List<RefTableVersionInfo> fetchPublishedRefVersionInfoList() {
        return refTableVersionRepository.fetchPublishedRefVersionInfoList();
    }


    List<RefTableVersionInfo> fetchRefVersionByTableId(Long tableId) {
        return refTableVersionRepository.selectRefVersionBytableId(tableId);
    }

    public RefTableVersionInfo getRefVersionInfo(Long versionId) {
        return refTableVersionRepository.selectRefVersionInfo(versionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefTableVersionInfo deactivateTableVersion(Long versionId) {
        RefTableVersionInfo refTableVersionInfo = getRefVersionInfo(versionId);
        if (!refTableVersionInfo.getPublished()) {
            log.warn("version is unPublished,version number:{}", refTableVersionInfo.getVersionNumber());
            throw new IllegalStateException(String.format("version is unPublished,version number:%s", refTableVersionInfo.getVersionNumber()));
        }
        OffsetDateTime now = DateTimeUtils.now();
        refTableVersionInfo.setPublished(false);
        refTableVersionInfo.setEndTime(now);
        refTableVersionInfo.setUpdateUser(getCurrentUsername());
        refTableVersionInfo.setUpdateTime(now);
        refTableVersionRepository.update(refTableVersionInfo);
        return refTableVersionInfo;
    }
}
