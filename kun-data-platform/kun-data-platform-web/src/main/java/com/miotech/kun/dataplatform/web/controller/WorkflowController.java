package com.miotech.kun.dataplatform.web.controller;

import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.workflow.client.WorkflowClient;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public RequestResult<Object> setMaintenanceMode(@RequestParam Boolean mode) {
        workflowClient.setMaintenanceMode(mode);
        return RequestResult.success(new AcknowledgementVO("Operation acknowledged."));
    }
}
