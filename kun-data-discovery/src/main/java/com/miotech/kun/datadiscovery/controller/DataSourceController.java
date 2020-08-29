package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.datadiscovery.model.bo.*;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.model.vo.DatasetLineageVO;
import com.miotech.kun.datadiscovery.model.vo.PullDataVO;
import com.miotech.kun.datadiscovery.service.DatasetFieldService;
import com.miotech.kun.datadiscovery.service.DatasetService;
import com.miotech.kun.datadiscovery.service.DataSourceService;
import com.miotech.kun.datadiscovery.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@RestController
@RequestMapping("/kun/api/v1")
@Slf4j
public class DataSourceController {

    @Autowired
    DatasetService datasetService;

    @Autowired
    DatasetFieldService datasetFieldService;

    @Autowired
    DataSourceService dataSourceService;

    @Autowired
    MetadataService metadataService;

    @GetMapping("/metadata/datasources/search")
    public RequestResult<DataSourceBasicPage> searchDataSource(BasicSearchRequest basicSearchRequest) {
        return RequestResult.success(dataSourceService.search(basicSearchRequest));
    }

    @GetMapping("/metadata/datasources")
    public RequestResult<DataSourcePage> getDataSource(DataSourceSearchRequest dataSourceSearchRequest) {
        return RequestResult.success(dataSourceService.search(dataSourceSearchRequest));
    }

    @PostMapping("/metadata/datasource/add")
    public RequestResult<DataSource> addDataSource(@RequestBody DataSourceRequest dataSourceRequest) throws SQLException {
        return RequestResult.success(dataSourceService.add(dataSourceRequest));
    }

    @PostMapping("/metadata/datasource/{id}/update")
    public RequestResult<DataSource> updateDataSource(@PathVariable Long id,
                                                      @RequestBody DataSourceRequest dataSourceRequest) throws SQLException {
        return RequestResult.success(dataSourceService.update(id, dataSourceRequest));
    }

    @DeleteMapping("/metadata/datasource/{id}")
    public RequestResult<IdVO> deleteDataSource(@PathVariable Long id) {
        dataSourceService.delete(id);
        IdVO idVO = new IdVO();
        idVO.setId(id);
        return RequestResult.success(idVO);
    }

    @PostMapping("/metadata/datasource/{id}/pull")
    public RequestResult<PullDataVO> pullDataSource(@PathVariable Long id) {
        metadataService.pullDataSource(id);
        return RequestResult.success();
    }

    @GetMapping("/metadata/datasource/types")
    public RequestResult<List<DataSourceType>> getDataSourceTypes() {
        return RequestResult.success(dataSourceService.getAllTypes());
    }

    @GetMapping("/metadata/databases")
    public RequestResult<List<Database>> getDatabases() {
        return RequestResult.success(datasetService.getAllDatabase());
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
        return RequestResult.success(datasetService.update(id, datasetRequest));
    }

    @PostMapping("/metadata/dataset/{id}/pull")
    public RequestResult<PullDataVO> pullDataset(@PathVariable Long id) {
        metadataService.pullDataset(id);
        return RequestResult.success();
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

    @GetMapping("/metadata/dataset/{id}/lineages")
    public RequestResult<DatasetLineageVO> getDatasetLineages(@PathVariable String id) {
        return RequestResult.success();
    }

}
