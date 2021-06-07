package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.bo.*;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.model.vo.PullProcessVO;
import com.miotech.kun.datadiscovery.service.DataSourceService;
import com.miotech.kun.datadiscovery.service.DatasetFieldService;
import com.miotech.kun.datadiscovery.service.DatasetService;
import com.miotech.kun.datadiscovery.service.MetadataService;
import com.miotech.kun.metadata.core.model.DatasetColumnHintRequest;
import com.miotech.kun.metadata.core.model.DatasetColumnHintResponse;
import com.miotech.kun.workflow.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author: Melo
 * @created: 5/26/20
 */

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
        return RequestResult.success(datasetService.getDatabases(request));
    }

    @GetMapping("/metadata/datasets/search")
    public RequestResult<DatasetBasicPage> searchDatasets(BasicSearchRequest basicSearchRequest) {
        return RequestResult.success(datasetService.search(basicSearchRequest));
    }

    @GetMapping("/metadata/datasets")
    public RequestResult<DatasetBasicPage> getDatasets(DatasetSearchRequest searchRequests) {
        return RequestResult.success(datasetService.search(searchRequests));
    }

    @GetMapping("/metadata/dataset/{id}")
    public RequestResult<Dataset> getDatasetDetail(@PathVariable Long id) {
        Dataset dataset = datasetService.find(id);
        return RequestResult.success(dataset);
    }

    @PostMapping("/metadata/dataset/{id}/update")
    public RequestResult<Dataset> updateDataset(@PathVariable Long id,
                                                @RequestBody DatasetRequest datasetRequest) {
        Dataset dataset = datasetService.update(id, datasetRequest);
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
        return RequestResult.success(datasetFieldService.find(id, searchRequest));
    }

    @PostMapping("/metadata/column/{id}/update")
    public RequestResult<DatasetField> updateDatasetColumn(@PathVariable Long id,
                                                           @RequestBody DatasetFieldRequest datasetFieldRequest) {
        return RequestResult.success(datasetFieldService.update(id, datasetFieldRequest));
    }

    @GetMapping("/metadata/dataset/database/_hint")
    public RequestResult<List<String>> hintDatabase(@RequestParam(required = false) String prefix) {
        return RequestResult.success(metadataService.hintDatabase(prefix));
    }

    @GetMapping("/metadata/dataset/table/_hint")
    public RequestResult<List<String>> hintTable(@RequestParam String databaseName, @RequestParam(required = false) String prefix) {
        return RequestResult.success(metadataService.hintTable(databaseName, prefix));
    }

    @PostMapping("/metadata/dataset/column/_hint")
    public RequestResult<List<DatasetColumnHintResponse>> hintColumn(@RequestBody List<DatasetColumnHintRequest> columnHintRequests) {
        return RequestResult.success(metadataService.hintColumn(columnHintRequests));
    }

}
