package com.miotech.kun.datadiscover.controller;

import com.miotech.kun.datadiscover.model.RequestResult;
import com.miotech.kun.datadiscover.model.bo.*;
import com.miotech.kun.datadiscover.model.entity.*;
import com.miotech.kun.datadiscover.model.vo.*;
import com.miotech.kun.datadiscover.service.DatasetFieldService;
import com.miotech.kun.datadiscover.service.DatasetService;
import com.miotech.kun.datadiscover.service.DatasourceService;
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
    DatasourceService datasourceService;

    @GetMapping("/metadata/databases/search")
    public RequestResult<DatasourceBasicPage> searchDatabases(BasicSearchRequest basicSearchRequest) {
        return RequestResult.success(datasourceService.search(basicSearchRequest));
    }

    @GetMapping("/metadata/databases")
    public RequestResult<DatasourcePage> getDatabases(DatabaseSearchRequest databaseSearchRequest) {
        return RequestResult.success(datasourceService.search(databaseSearchRequest));
    }

    @PostMapping("/metadata/database/add")
    public RequestResult<Datasource> addDatabase(@RequestBody DatabaseRequest databaseRequest) throws SQLException {
        return RequestResult.success(datasourceService.add(databaseRequest));
    }

    @PostMapping("/metadata/database/{id}/update")
    public RequestResult<Datasource> updateDatabase(@PathVariable Long id,
                                                    @RequestBody DatabaseRequest databaseRequest) throws SQLException {
        return RequestResult.success(datasourceService.update(id, databaseRequest));
    }

    @DeleteMapping("/metadata/database/{id}")
    public RequestResult<IdVO> deleteDatabase(@PathVariable Long id) {
        datasourceService.delete(id);
        IdVO idVO = new IdVO();
        idVO.setId(id);
        return RequestResult.success(idVO);
    }

    @PostMapping("/metadata/database/{id}/pull")
    public RequestResult<PullDataVO> pullDatabase(@PathVariable String id) {
        RequestResult<PullDataVO> requestResult = RequestResult.success();
        return requestResult;
    }

    @GetMapping("/metadata/database/types")
    public RequestResult<List<DatasourceType>> getDatasourceTypes() {
        return RequestResult.success(datasourceService.getAllTypes());
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
    public RequestResult<PullDataVO> pullDataset(@PathVariable String id) {
        RequestResult<PullDataVO> requestResult = RequestResult.success();
        return requestResult;
    }

    @GetMapping("/metadata/dataset/{id}/columns")
    public RequestResult<DatasetColumnListVO> getDatasetColumns(@PathVariable Long id) {
        DatasetColumnListVO vo = new DatasetColumnListVO();
        vo.setColumns(datasetFieldService.find(id));
        return RequestResult.success(vo);
    }

    @PostMapping("/metadata/column/{id}/update")
    public RequestResult<DatasetColumn> updateDatasetColumn(@PathVariable Long id,
                                                            @RequestBody DatasetColumnRequest datasetColumnRequest) {
        return RequestResult.success(datasetFieldService.update(id, datasetColumnRequest));
    }

    @GetMapping("/metadata/dataset/{id}/lineages")
    public RequestResult<DatasetLineageVO> getDatasetLineages(@PathVariable String id) {
        RequestResult<DatasetLineageVO> requestResult = RequestResult.success();
        return requestResult;
    }

}
