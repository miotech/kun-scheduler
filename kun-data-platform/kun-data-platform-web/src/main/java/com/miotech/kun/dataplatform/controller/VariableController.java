package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.constant.VariableNamespace;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.VariableVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@Api(tags = "Variables")
@Slf4j
public class VariableController {
    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private VariableNamespace variableNamespace;

    @GetMapping("/variables")
    public RequestResult<List<VariableVO>> fetchAllVariables() {
        try {
            List<VariableVO> variableVOS = workflowClient.getAllVariables();
            return RequestResult.success(variableVOS);
        } catch (Exception e) {
            return RequestResult.error(e.getMessage());
        }
    }

    @PostMapping("/variables")
    public RequestResult<VariableVO> createVariable(@RequestBody VariableVO createVO) {
        try {
            VariableVO resultVO = workflowClient.createVariable(createVO);
            return RequestResult.success(resultVO);
        } catch (Exception e) {
            return RequestResult.error(e.getMessage());
        }
    }

    @PutMapping("/variables")
    public RequestResult<VariableVO> updateVariable(@RequestBody VariableVO createVO) {
        try {
            VariableVO resultVO = workflowClient.updateVariable(createVO);
            return RequestResult.success(resultVO);
        } catch (Exception e) {
            return RequestResult.error(e.getMessage());
        }
    }

    @DeleteMapping("/variables/{key}")
    public RequestResult<Boolean> deleteVariable(@PathVariable String key) {
        try {
            Boolean successFlag = workflowClient.deleteVariable(variableNamespace.toString(), key);
            return RequestResult.success(successFlag);
        } catch (Exception e) {
            return RequestResult.error(e.getMessage());
        }
    }
}
