package com.miotech.kun.dataplatform.web.controller;

import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.ExecutorInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@Api(tags ="Workflow")
@Slf4j
public class WorkflowController {

    @Autowired
    private WorkflowClient workflowClient;

    @GetMapping("/workflow/getMaintenanceMode")
    public RequestResult<Boolean> getMaintenanceMode() {
        return RequestResult.success(workflowClient.getMaintenanceMode());
    }

    @PostMapping("/workflow/setMaintenanceMode")
    @OperationRecord(type = OperationRecordType.WORKFLOW_SET_MAINTENANCE_MODE, args = {"#mode"})
    public RequestResult<Object> setMaintenanceMode(@RequestParam Boolean mode) {
        workflowClient.setMaintenanceMode(mode);
        return RequestResult.success(new AcknowledgementVO("Operation acknowledged."));
    }

    @GetMapping("/workflow/executorInfo")
    public RequestResult<List<ExecutorInfo>> getExecutorInfo() {
        ExecutorInfo executorInfo = workflowClient.getExecutorInfo();
        List<ExecutorInfo> result = new ArrayList<>();
        if (executorInfo.getExtraInfo() != null) {
            for (ExecutorInfo e : executorInfo.getExtraInfo().values()) {
                if (e != null) {
                    result.add(e);
                }
            }
        } else {
            result.add(executorInfo);
        }
        return RequestResult.success(result);
    }
}
