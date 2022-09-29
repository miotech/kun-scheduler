package com.miotech.kun.datadiscovery.controller;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageRequest;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.bo.*;
import com.miotech.kun.datadiscovery.model.entity.Database;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefUpdateResult;
import com.miotech.kun.datadiscovery.model.vo.BaseRefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.vo.RefDataVersionFillInfo;
import com.miotech.kun.datadiscovery.model.vo.RefTableVersionFillInfo;
import com.miotech.kun.datadiscovery.model.vo.ValidationResultVo;
import com.miotech.kun.datadiscovery.service.MetadataService;
import com.miotech.kun.datadiscovery.service.RdmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final MetadataService metadataService;

    @Value("${rdm.datasource:0}")
    private Long datasourceId;

    @GetMapping("/rdm/databases")
    public RequestResult<List<Database>> getDatabases() {
        DatabaseRequest request = new DatabaseRequest();
        request.setDataSourceIds(ImmutableList.of(datasourceId));
        return RequestResult.success(metadataService.getDatabases(request));
    }

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
     * 检查数据
     *
     * @param request
     * @return
     */
    @PostMapping("/rdm/valid")
    public RequestResult<ValidationResultVo> validRefData(@RequestBody ValidRefDataRequest request) {
        ValidationResultVo validationResultVo = rdmService.validRefDataInfo(request);
        return RequestResult.success(validationResultVo);
    }

    /**
     * 编辑ref table info
     *
     * @param request
     * @return
     */
    @PostMapping("/rdm/edit")
    public RequestResult<RefUpdateResult> editRefDataInfo(@RequestBody EditRefDataTableRequest request) {
        RefUpdateResult refUpdateResult = rdmService.editRefDataInfo(request);
        return RequestResult.success(refUpdateResult);
    }

    /**
     * 获取ref 版本日志
     *
     * @param tableId
     * @return
     */
    @GetMapping("/rdm/info/{tableId}")
    public RequestResult<List<BaseRefTableVersionInfo>> fetchRefVersionByTableId(@PathVariable(value = "tableId") Long tableId) {
        List<BaseRefTableVersionInfo> baseRefTableVersionInfos = rdmService.fetchRefVersionList(tableId);
        return RequestResult.success(baseRefTableVersionInfos);
    }

    /**
     * 存在未发布返回未发布，如果不存在返回发布的版本
     *
     * @param tableId
     * @return
     */
    @GetMapping("/rdm/data/edit/{tableId}")
    public RequestResult<RefDataVersionFillInfo> getEditableRefDataInfo(@PathVariable(value = "tableId") Long tableId) {
        RefDataVersionFillInfo refDataVersionFillInfo = rdmService.fetchEditableRefDataVersionInfo(tableId);
        return RequestResult.success(refDataVersionFillInfo);
    }


    /**
     * ref table page list
     *
     * @param pageRequest
     * @return
     */
    @Deprecated
    @GetMapping("/rdm/info/table/page")
    public RequestResult<PageResult> pageRefTableInfo(PageRequest pageRequest) {
        PageResult<RefTableVersionFillInfo> refTableVersionFillInfoPageResult = rdmService.pageRefTableInfo(pageRequest);
        return RequestResult.success(refTableVersionFillInfoPageResult);
    }

    @PostMapping("/rdm/info/table/page/search")
    public RequestResult<PageResult> pageRefTableInfoBySearch(@RequestBody BasicSearchRequest searchRequest) {
        PageResult<RefTableVersionFillInfo> refTableVersionFillInfoPageResult = rdmService.pageRefTableInfoBySearch(searchRequest);
        return RequestResult.success(refTableVersionFillInfoPageResult);
    }

    @PostMapping("/rdm/attribute/list")
    public RequestResult<List<String>> getResourceAttributeList(@RequestBody ResourceAttributeRequest request) {
        return RequestResult.success(rdmService.fetchResourceAttributeList(request));
    }

    /**
     * 获取指定版本的ref data
     *
     * @param versionId
     * @return
     */
    @GetMapping("/rdm/data/{versionId}")
    public RequestResult<RefDataVersionFillInfo> getVersionData(@PathVariable(value = "versionId") Long versionId) {
        RefDataVersionFillInfo refDataVersionFillInfo = rdmService.fetchRefDataVersionInfo(versionId);
        return RequestResult.success(refDataVersionFillInfo);
    }


    /**
     * 发布指定版本
     *
     * @param versionId
     * @return
     */
    @PostMapping("/rdm/publish/{versionId}")
    public RequestResult<Boolean> publishRefDataTableVersion(@PathVariable(value = "versionId") Long versionId) {
        boolean publishVersion = rdmService.publishRefDataTableVersion(versionId);
        return RequestResult.success(publishVersion);
    }


    /**
     * 回滚指定版本
     *
     * @param versionId
     * @return
     */
    @PostMapping("/rdm/rollback/{versionId}")
    public RequestResult<Boolean> rollBackRefDataTableVersion(@PathVariable(value = "versionId") Long versionId) {
        boolean rollBack = rdmService.rollbackRefDataTableVersion(versionId);
        return RequestResult.success(rollBack);
    }

    /**
     * 停用
     *
     * @param versionId
     * @return
     */
    @PostMapping("/rdm/deactivate/{versionId}")
    public RequestResult<Boolean> deactivateRefDataTableVersion(@PathVariable(value = "versionId") Long versionId) {
        boolean deactivate = rdmService.deactivateRefDataTableVersion(versionId);
        return RequestResult.success(deactivate);
    }

}
