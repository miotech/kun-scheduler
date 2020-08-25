package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.common.datastore.service.DatasetService;
import com.miotech.kun.dataplatform.common.datastore.vo.DatasetSearchRequest;
import com.miotech.kun.dataplatform.common.datastore.vo.DatasetVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
@Api(tags = "Data")
@Slf4j
public class DataController {

    @Autowired
    private DatasetService datasetService;

    @GetMapping("/data-sets/_search")
    @ApiOperation("Get dataset and related task definition")
    public RequestResult<List<DatasetVO>> getDeployedTaskDAG(@RequestParam(required = false) String name,
                                                             @RequestParam(required =  false) List<Long> definitionIds,
                                                             @RequestParam(required =  false) List<Long> datastoreIds) {
        DatasetSearchRequest request = new DatasetSearchRequest(
                definitionIds,
                datastoreIds,
                name
        );
        return RequestResult.success(datasetService.searchDatasets(request));
    }
}
