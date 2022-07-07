package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.bo.EditRefDataTableRequest;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefUpdateResult;
import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.vo.RefTableVersionFillInfo;
import com.miotech.kun.datadiscovery.service.RdmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @program: kun
 * @description: RefDataController
 * @author: zemin  huang
 * @create: 2022-06-30 14:10
 **/

@RestController
@RequestMapping("/kun/api/v1")
@Slf4j
@RequiredArgsConstructor
public class RdmController {

    private final RdmService rdmService;

    /**
     * 数据上传解析
     *
     * @param file
     * @return
     */
    @PostMapping("/rdm/data/parse")
    public RequestResult<RefBaseTable> parseFile(@RequestBody MultipartFile file) {
        RefBaseTable refBaseTable = rdmService.parseFile(file);
        return RequestResult.success(refBaseTable);
    }

    /**
     * 编辑ref table info
     *
     * @param request
     * @return
     */
    @PostMapping("/rdm/edit")
    public RequestResult<RefUpdateResult> updateRefDataInfo(@RequestBody EditRefDataTableRequest request) {
        RefUpdateResult refUpdateResult = rdmService.updateUnpublishedRefDataInfo(request);
        return RequestResult.success(refUpdateResult);
    }

    /**
     * 获取ref 版本日志
     *
     * @param tableId
     * @return
     */
    @GetMapping("/rdm/info/{tableId}")
    public RequestResult<List<BaseRefTableVersionInfo>> getRefDataVersion(@PathVariable(value = "tableId") Long tableId) {
        List<BaseRefTableVersionInfo> baseRefTableVersionInfos = rdmService.fetchRefDataVersion(tableId);
        return RequestResult.success(baseRefTableVersionInfos);
    }

    @GetMapping("/rdm/info/publish_list")
    public RequestResult<List<RefTableVersionFillInfo>> getPublishTableList() {
        List<RefTableVersionFillInfo> refTableVersionFillInfos = rdmService.fetchPublishedRefVersionInfoList();
        return RequestResult.success(refTableVersionFillInfos);
    }

    /**
     * 获取指定版本的ref data
     *
     * @param versionId
     * @return
     */
    @GetMapping("/rdm/data/{versionId}")
    public RequestResult<RefBaseTable> getLatestVersionData(@PathVariable(value = "versionId") Long versionId) {
        RefBaseTable refBaseTable = rdmService.fetchVersionData(versionId);
        return RequestResult.success(refBaseTable);
    }

    /**
     * 发布指定版本
     *
     * @param versionId
     * @return
     */
    @PostMapping("/rdm/publish/{versionId}")
    public RequestResult<Boolean> openRefDataTableVersion(@PathVariable(value = "versionId") Long versionId) {
        boolean publishVersion = rdmService.activateRefDataTableVersion(versionId);
        return RequestResult.success(publishVersion);
    }

    /**
     * 停用
     *
     * @param versionId
     * @return
     */

    @PostMapping("/rdm/deactivate/{versionId}")
    public RequestResult<Boolean> deactivateRefDataTableVersion(@PathVariable(value = "versionId") Long versionId) {
        boolean rollback = rdmService.deactivateRefDataTableVersion(versionId);
        return RequestResult.success(rollback);
    }

}
