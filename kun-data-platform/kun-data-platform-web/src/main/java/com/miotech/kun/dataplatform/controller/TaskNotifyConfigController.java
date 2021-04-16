package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.common.notifyconfig.service.TaskNotifyConfigService;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/")
@Api(tags = "TaskNotifyConfig")
@Slf4j
public class TaskNotifyConfigController {
    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    @GetMapping("/task-notify-config/{configId}")
    @ApiOperation("Fetch a task notification config by its id")
    public RequestResult<TaskNotifyConfig> fetchTaskNotifyConfigById(@PathVariable("configId") Long configId) {
        Optional<TaskNotifyConfig> taskNotifyConfig = taskNotifyConfigService.fetchTaskNotifyConfigById(configId);
        return taskNotifyConfig
                .map(RequestResult::success)
                .orElseGet(() -> RequestResult.error(404, String.format("Cannot find task notify config with id = %s", configId)));
    }

    @GetMapping("/task-notify-config/workflow-task/{workflowTaskId}")
    @ApiOperation("Fetch a task notification config by its bound workflow task id")
    public RequestResult<TaskNotifyConfig> fetchTaskNotifyConfigByWorkflowTaskId(@PathVariable("workflowTaskId") Long workflowTaskId) {
        Optional<TaskNotifyConfig> taskNotifyConfig = taskNotifyConfigService.fetchTaskNotifyConfigByWorkflowTaskId(workflowTaskId);
        return taskNotifyConfig
                .map(RequestResult::success)
                .orElseGet(() -> RequestResult.error(404, String.format("Cannot find task notify config with workflow task id = %s", workflowTaskId)));
    }

    @PutMapping("/task-notify-config")
    @ApiOperation("Update or insert a task notification config by its id")
    public RequestResult<TaskNotifyConfig> updateTaskNotifyConfig(TaskNotifyConfig taskNotifyConfig) {
        TaskNotifyConfig taskNotifyConfigUpdated = taskNotifyConfigService.upsertTaskNotifyConfig(taskNotifyConfig);
        return RequestResult.success(taskNotifyConfigUpdated);
    }

    @DeleteMapping("/task-notify-config/{configId}")
    @ApiOperation("Delete a task notification config by its id")
    public RequestResult<AcknowledgementVO> deleteTaskNotifyConfig(@PathVariable("configId")  Long configId) {
        boolean removed = taskNotifyConfigService.removeTaskNotifyConfigById(configId);
        return RequestResult.success(new AcknowledgementVO(removed ? "Remove success" : "Cannot find target notify config"));
    }

    @DeleteMapping("/task-notify-config/workflow-task/{workflowTaskId}")
    @ApiOperation("Delete a task notification config by its id")
    public RequestResult<AcknowledgementVO> deleteTaskNotifyConfigByWorkflowTaskId(@PathVariable("workflowTaskId") Long workflowTaskId) {
        boolean removed = taskNotifyConfigService.removeTaskNotifyConfigByWorkflowTaskId(workflowTaskId);
        return RequestResult.success(new AcknowledgementVO(removed ? "Remove success" : "Cannot find target notify config"));
    }
}
