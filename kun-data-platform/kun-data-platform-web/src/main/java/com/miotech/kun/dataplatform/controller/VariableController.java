package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.model.variable.VariableRemovalInfoVO;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.VariableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class VariableController {
    @Autowired
    private WorkflowClient workflowClient;

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

    @DeleteMapping("/variables")
    public RequestResult<Boolean> deleteVariable(@RequestBody VariableRemovalInfoVO removalInfoVO) {
        try {
            Boolean successFlag = workflowClient.deleteVariable(removalInfoVO.getNamespace(), removalInfoVO.getKey());
            return RequestResult.success(successFlag);
        } catch (Exception e) {
            return RequestResult.error(e.getMessage());
        }
    }
}
