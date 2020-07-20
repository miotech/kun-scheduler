package com.miotech.kun.dataquality.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.dataquality.model.bo.DataQualityRequest;
import com.miotech.kun.dataquality.model.bo.DeleteDataQualityRequest;
import com.miotech.kun.dataquality.model.bo.ValidateSqlRequest;
import com.miotech.kun.dataquality.model.entity.DataQualityCase;
import com.miotech.kun.dataquality.model.entity.DimensionConfig;
import com.miotech.kun.dataquality.model.entity.ValidateSqlResult;
import com.miotech.kun.dataquality.service.DataQualityService;
import com.miotech.kun.dataquality.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@RestController
@RequestMapping("/kun/api/v1")
public class DataQualityController {

    @Autowired
    DataQualityService dataQualityService;

    @Autowired
    WorkflowService workflowService;

    @GetMapping("/data-quality/dimension/get-config")
    public RequestResult<DimensionConfig> getDimensionConfig(@RequestParam("datasourceType") String dsType) {
        return RequestResult.success(dataQualityService.getDimensionConfig(dsType));
    }

    @PostMapping("/data-quality/add")
    public RequestResult<IdVO> addCase(@RequestBody DataQualityRequest dataQualityRequest) {
        IdVO vo = new IdVO();
        vo.setId(dataQualityService.addCase(dataQualityRequest));
        Long taskId = workflowService.executeTask(vo.getId());
        dataQualityService.saveTaskId(vo.getId(), taskId);
        return RequestResult.success(vo);
    }

    @GetMapping("/data-quality/{id}")
    public RequestResult<DataQualityCase> getCase(@PathVariable("id") Long id) {
        return RequestResult.success(dataQualityService.getCase(id));
    }

    @PostMapping("/sql/validate")
    public RequestResult<ValidateSqlResult> validateSql(@RequestBody ValidateSqlRequest request) {
        return RequestResult.success(dataQualityService.validateSql(request));
    }

    @PostMapping("/data-quality/{id}/edit")
    public RequestResult<IdVO> updateCase(@PathVariable("id") Long id,
                                          @RequestBody DataQualityRequest dataQualityRequest) {
        IdVO vo = new IdVO();
        vo.setId(dataQualityService.updateCase(id, dataQualityRequest));
        Long taskId = workflowService.executeTask(vo.getId());
        dataQualityService.saveTaskId(vo.getId(), taskId);
        return RequestResult.success(vo);
    }

    @DeleteMapping("/data-quality/{id}/delete")
    public RequestResult<IdVO> deleteCase(@PathVariable("id") Long id,
                                          @RequestBody DeleteDataQualityRequest request) {
        workflowService.deleteTask(id);
        IdVO vo = new IdVO();
        vo.setId(dataQualityService.deleteCase(id, request.getDatasetId()));
        return RequestResult.success(vo);
    }
}
