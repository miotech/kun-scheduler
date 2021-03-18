package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.config.WorkflowConfig;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.VariableUpsertVO;
import com.miotech.kun.workflow.client.model.VariableVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Api(tags = "Variables")
@Slf4j
public class VariableController {
    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private WorkflowConfig workflowConfig;

    @GetMapping("/variables")
    public RequestResult<List<VariableVO>> fetchAllVariables() {
        List<VariableVO> variableVOS = workflowClient.getAllVariables()
                .stream()
                .filter(variable -> Objects.equals(variable.getNamespace(), workflowConfig.getVariableNamespace()))
                .collect(Collectors.toList());
        return RequestResult.success(variableVOS);
    }

    @PostMapping("/variables")
    public RequestResult<VariableVO> createVariable(@RequestBody VariableUpsertVO createVO) {
        VariableVO resultVO = workflowClient.createVariable(convertUpsertVO(createVO));
        return RequestResult.success(resultVO);
    }

    @PutMapping("/variables")
    public RequestResult<VariableVO> updateVariable(@RequestBody VariableUpsertVO createVO) {
        VariableVO resultVO = workflowClient.updateVariable(convertUpsertVO(createVO));
        return RequestResult.success(resultVO);
    }

    @DeleteMapping("/variables/{key}")
    public RequestResult<Boolean> deleteVariable(@PathVariable String key) {
        Boolean successFlag = workflowClient.deleteVariable(workflowConfig.getVariableNamespace(), key);
        return RequestResult.success(successFlag);
    }

    private VariableVO convertUpsertVO(VariableUpsertVO upsertVO) {
        return VariableVO.newBuilder()
                .withNamespace(workflowConfig.getVariableNamespace())
                .withKey(upsertVO.getKey())
                .withValue(upsertVO.getValue())
                .withEncrypted(upsertVO.isEncrypted())
                .build();
    }
}
