package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DataSource;
import com.miotech.kun.datadiscovery.model.entity.DataSourceBasicPage;
import com.miotech.kun.datadiscovery.model.entity.DataSourcePage;
import com.miotech.kun.datadiscovery.model.entity.DataSourceType;
import com.miotech.kun.datadiscovery.model.vo.PullProcessVO;
import com.miotech.kun.datadiscovery.service.DataSourceService;
import com.miotech.kun.datadiscovery.service.DatasetFieldService;
import com.miotech.kun.datadiscovery.service.DatasetService;
import com.miotech.kun.datadiscovery.service.MetadataService;
import com.miotech.kun.workflow.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

    @Autowired
    WorkflowClient workflowClient;

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
    public RequestResult<PullProcessVO> pullDataSource(@PathVariable Long id) {
        PullProcessVO vo = metadataService.pullDataSource(id);
        return RequestResult.success(vo);
    }

    @GetMapping("/metadata/datasource/processes/latest")
    public RequestResult<Map<String, PullProcessVO>> pullDataset(@RequestParam List<Long> dataSourceIds) {
        Map<String, PullProcessVO> map = metadataService.fetchLatestPullProcessByDataSourceIds(dataSourceIds);
        return RequestResult.success(map);
    }

    @GetMapping("/metadata/datasource/types")
    public RequestResult<List<DataSourceType>> getDataSourceTypes() {
        return RequestResult.success(dataSourceService.getAllTypes());
    }

}
