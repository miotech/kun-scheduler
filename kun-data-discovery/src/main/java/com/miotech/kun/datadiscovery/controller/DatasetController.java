package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.bo.*;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.model.vo.PullProcessVO;
import com.miotech.kun.datadiscovery.service.*;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestRequest;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestResponse;
import com.miotech.kun.workflow.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/kun/api/v1")
@Slf4j
public class DatasetController {

    @Autowired
    DatasetService datasetService;

    @Autowired
    DatasetFieldService datasetFieldService;

    @Autowired
    DataSourceService dataSourceService;

    @Autowired
    MetadataService metadataService;

    @Autowired
    WorkflowClient workflowClient;

    @GetMapping("/metadata/databases")
    public RequestResult<List<Database>> getDatabases(DatabaseRequest request) {
        return RequestResult.success(metadataService.getDatabases(request));
    }

    @GetMapping("/metadata/datasets/search")
    public RequestResult<DatasetBasicPage> searchDatasets(BasicSearchRequest basicSearchRequest) {
        return RequestResult.success(metadataService.searchDatasets(basicSearchRequest));
    }

    @GetMapping("/metadata/datasets")
    public RequestResult<DatasetBasicPage> getDatasets(DatasetSearchRequest searchRequests) {
        return RequestResult.success(metadataService.fullTextSearch(searchRequests));
    }

    @GetMapping("/metadata/dataset/{id}")
    public RequestResult<Dataset> getDatasetDetail(@PathVariable Long id) {
        Dataset dataset = metadataService.findById(id);
        return RequestResult.success(dataset);
    }

    @PostMapping("/metadata/dataset/{id}/update")
    public RequestResult<Dataset> updateDataset(@PathVariable Long id,
                                                @RequestBody DatasetRequest datasetRequest) {
        Dataset dataset = metadataService.updateDataSet(id, datasetRequest);
        return RequestResult.success(dataset);
    }

    @PostMapping("/metadata/dataset/{id}/pull")
    public RequestResult<PullProcessVO> pullDataset(@PathVariable Long id) {
        PullProcessVO vo = metadataService.pullDataset(id);
        return RequestResult.success(vo);
    }

    @GetMapping("/metadata/dataset/{id}/pull/latest")
    public RequestResult<PullProcessVO> getDatasetLatestPullProcess(@PathVariable Long id) {
        Optional<PullProcessVO> voOptional = metadataService.fetchLatestPullProcessForDataset(id);
        return RequestResult.success(voOptional.orElse(null));
    }

    @GetMapping("/metadata/dataset/{id}/columns")
    public RequestResult<DatasetFieldPage> getDatasetColumns(@PathVariable Long id,
                                                             DatasetFieldSearchRequest searchRequest) {
        return RequestResult.success(metadataService.findColumns(id, searchRequest));
    }

    @PostMapping("/metadata/column/{id}/update")
    public RequestResult<DatasetField> updateDatasetColumn(@PathVariable Long id,
                                                           @RequestBody DatasetFieldRequest datasetFieldRequest) {
        return RequestResult.success(metadataService.updateColumn(id, datasetFieldRequest));
    }

    @GetMapping("/metadata/dataset/database/_suggest")
    public RequestResult<List<String>> suggestDatabase(@RequestParam(required = false) String prefix) {
        return RequestResult.success(metadataService.suggestDatabase(prefix));
    }

    @GetMapping("/metadata/dataset/table/_suggest")
    public RequestResult<List<String>> suggestTable(@RequestParam String databaseName, @RequestParam(required = false) String prefix) {
        return RequestResult.success(metadataService.suggestTable(databaseName, prefix));
    }

    @PostMapping("/metadata/dataset/column/_suggest")
    public RequestResult<List<DatasetColumnSuggestResponse>> suggestColumn(@RequestBody List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        return RequestResult.success(metadataService.suggestColumn(columnSuggestRequests));
    }

}
