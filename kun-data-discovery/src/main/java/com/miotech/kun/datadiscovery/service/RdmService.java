package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.bo.EditRefDataTableRequest;
import com.miotech.kun.datadiscovery.model.bo.EditRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.vo.RefTableVersionFillInfo;
import com.miotech.kun.datadiscovery.service.rdm.RefDataOperator;
import com.miotech.kun.datadiscovery.service.rdm.RefValidator;
import com.miotech.kun.datadiscovery.service.rdm.file.RefStorageFileBuilder;
import com.miotech.kun.datadiscovery.service.rdm.file.RefUploadFileBuilder;
import com.miotech.kun.datadiscovery.service.rdm.file.S3StorageFileManger;
import difflib.DiffUtils;
import difflib.Patch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-20 17:30
 **/
@RequiredArgsConstructor
@Service
@Slf4j
public class RdmService {

    private final RefTableVersionService refTableVersionService;
    private final RefUploadFileBuilder refUploadFileBuilder;
    private final RefStorageFileBuilder refStorageFileBuilder;
    private final RefValidator refValidator;
    private final RefDataOperator refDataOperator;
    private final GlossaryService glossaryService;
    private final LineageAppService lineageAppService;

    @Transactional(rollbackFor = Exception.class)
    public RefBaseTable parseFile(MultipartFile file) {
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
            e.printStackTrace();
        }
        return refBaseTable;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefUpdateResult updateUnpublishedRefDataInfo(EditRefDataTableRequest request) {
        EditRefTableVersionInfo editRefVersionInfo = request.getEditRefTableVersionInfo();
        RefBaseTable refBaseTable = request.getRefBaseTable();
        ValidationResult validationResult = refValidator.valid(refBaseTable);
        if (!validationResult.getStatus()) {
            return RefUpdateResult.error(validationResult.getValidationMessageList());
        }
        RefTableVersionInfo version = null;
        RefTableVersionInfo refTableVersionInfo = new RefTableVersionInfo();
        refTableVersionInfo.setTableName(editRefVersionInfo.getTableName());
        refTableVersionInfo.setVersionDescription(editRefVersionInfo.getVersionDescription());
        refTableVersionInfo.setGlossaryList(editRefVersionInfo.getGlossaryList());
        refTableVersionInfo.setOwnerList(editRefVersionInfo.getOwnerList());
        refTableVersionInfo.setRefTableColumns(refBaseTable.getRefTableMetaData().getColumns());
        refTableVersionInfo.setRefTableConstraints(refBaseTable.getRefTableMetaData().getRefTableConstraints());
        Long tableId = request.getTableId();
        Long versionId = request.getVersionId();
        if (Objects.nonNull(versionId) && Objects.nonNull(tableId)) {
            version = refTableVersionService.updateRefDataVersion(tableId, versionId, refTableVersionInfo);
        } else {
            version = refTableVersionService.createInitRefDataVersion(refTableVersionInfo);
        }
        try {
            refStorageFileBuilder.override(new StorageFileData(version.getDataPath(), refBaseTable.getRefData()));
        } catch (IOException e) {
            log.error("Data writing failed ,edit base info:{}", editRefVersionInfo);
            throw new IllegalStateException("Data writing failed", e);
        }

        return RefUpdateResult.success(new BaseRefTableVersionInfo(version));

    }

    public RefBaseTable fetchVersionData(Long versionId) {
        RefTableVersionInfo refTableVersionInfo = refTableVersionService.getRefVersionInfo(versionId);
        StorageFileData storageFileData;
        try {
            storageFileData = refStorageFileBuilder.read(refTableVersionInfo.getDataPath());
        } catch (IOException e) {
            log.error(" storage data read error,version id :{}", versionId, e);
            throw new DataAccessResourceFailureException("data read error,version id:" + versionId, e);
        }
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        refTableMetaData.setColumns(refTableVersionInfo.getRefTableColumns());
        refTableMetaData.setRefTableConstraints(refTableVersionInfo.getRefTableConstraints());
        RefData refData = storageFileData.getRefData();
        return new RefBaseTable(refTableMetaData, refData);
    }

    public List<BaseRefTableVersionInfo> fetchRefDataVersion(Long tableId) {

        return refTableVersionService.fetchRefVersionByTableId(tableId).stream().map(BaseRefTableVersionInfo::new).collect(Collectors.toList());
    }

    public List<RefTableVersionFillInfo> fetchPublishedRefVersionInfoList() {
        List<RefTableVersionInfo> refTableVersionInfos = refTableVersionService.fetchPublishedRefVersionInfoList();
        return refTableVersionInfos.stream().map(refTableVersionInfo -> {
            RefTableVersionFillInfo refTableVersionFillInfo = new RefTableVersionFillInfo(refTableVersionInfo);
            List<String> glossaryList = refTableVersionInfo.getGlossaryList().stream().map(id -> glossaryService.getGlossaryBasicInfo(id).getName()).collect(Collectors.toList());
            refTableVersionFillInfo.setGlossaryList(glossaryList);
            Long datasetId = refTableVersionInfo.getDatasetId();
            // TODO: 2022/7/6 下周做
//            LineageGraphRequest lineageGraphRequest = new LineageGraphRequest();
//            lineageGraphRequest.setDatasetGid(datasetId);
//            lineageAppService.getLineageGraph(lineageGraphRequest).
//            refTableVersionFillInfo.setLinkTableList();
            return refTableVersionFillInfo;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean activateRefDataTableVersion(Long versionId) {
        RefTableVersionInfo versionInfo = refTableVersionService.activateRefDataTableVersion(versionId);
        refDataOperator.override(versionInfo);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deactivateRefDataTableVersion(Long versionId) {
        RefTableVersionInfo versionInfo = refTableVersionService.deactivateTableVersion(versionId);
        refDataOperator.deactivate(versionInfo.getTableName());
        return true;
    }

    // TODO: 2022/7/6   Need to determine requirements
    public DiffRefTableVersion diffVersion(Long originalVersion, Long revisedVersion) throws IOException {
        RefTableVersionInfo refVersionInfoOriginalDataVersion = refTableVersionService.getRefVersionInfo(originalVersion);
        RefTableVersionInfo refVersionInfoRevisedDataVersion = refTableVersionService.getRefVersionInfo(revisedVersion);
        StorageFileData originalStorageFileData = refStorageFileBuilder.read(refVersionInfoOriginalDataVersion.getDataPath());
        StorageFileData revisedStorageFileData = refStorageFileBuilder.read(refVersionInfoRevisedDataVersion.getDataPath());
        Patch<DataRecord> diff = DiffUtils.diff(originalStorageFileData.getRefData().getData(), revisedStorageFileData.getRefData().getData());
        return null;
    }


}
