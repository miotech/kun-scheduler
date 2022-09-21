package com.miotech.kun.dataplatform.web.controller;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.facade.model.deploy.Deploy;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskTry;
import com.miotech.kun.dataplatform.web.common.commit.service.TaskCommitService;
import com.miotech.kun.dataplatform.web.common.commit.vo.CommitRequest;
import com.miotech.kun.dataplatform.web.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.web.common.deploy.vo.DeployVO;
import com.miotech.kun.dataplatform.web.common.deploy.vo.TaskTryRunSearchRequest;
import com.miotech.kun.dataplatform.web.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.*;
import com.miotech.kun.dataplatform.web.common.taskdefview.service.TaskDefinitionViewService;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunLogRequest;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/")
@Api(tags = "TaskDefinition")
@Slf4j
public class TaskDefinitionController {

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private TaskDefinitionViewService taskDefinitionViewService;

    @Autowired
    private TaskCommitService taskCommitService;

    @Autowired
    private DeployService deployService;

    /*-------- task definitions -------*/

    @PostMapping("/task-definitions")
    @ApiOperation("Create TaskDefinition")
    public RequestResult<TaskDefinitionVO> createTaskDefinition(@RequestBody CreateTaskDefinitionRequest props) {
        TaskDefinition taskDefinition = taskDefinitionService.create(props);
        return RequestResult.success(taskDefinitionService.convertToVO(taskDefinition));
    }

    @GetMapping("/task-definitions/_search")
    @ApiOperation("Search TaskDefinitions")
    public RequestResult<PaginationResult<TaskDefinitionVO>> searchAllTaskDefinitions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(required = false) List<String> owners,
            @RequestParam(required = false) List<Long> definitionIds,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String taskTemplateName,
            @RequestParam(required = false) Optional<Boolean> archived,
            @RequestParam(required = false) List<Long> viewIds
    ) {
        TaskDefinitionSearchRequest searchRequest = new TaskDefinitionSearchRequest(
                pageSize,
                pageNum,
                name,
                taskTemplateName,
                definitionIds,
                owners,
                archived,
                viewIds
        );
        PaginationResult<TaskDefinition> taskDefinitions = taskDefinitionService.search(searchRequest);
        PaginationResult<TaskDefinitionVO> result = new PaginationResult<>(
                taskDefinitions.getPageSize(),
                taskDefinitions.getPageNum(),
                taskDefinitions.getTotalCount(),
                taskDefinitionService.convertToVOList(taskDefinitions.getRecords(), true)
        );
        return RequestResult.success(result);
    }

    @GetMapping("/task-definitions")
    @ApiOperation("List all TaskDefinitions")
    public RequestResult<List<TaskDefinitionVO>> listAllTaskDefinitions() {
        List<TaskDefinition> taskDefintions = taskDefinitionService.findAll();
        return RequestResult.success(taskDefinitionService.convertToVOList(taskDefintions, false));
    }

    @PutMapping("/task-definitions/{id}")
    @ApiOperation("Update TaskDefinition")
    public RequestResult<TaskDefinitionVO> updateTaskDefinitionDetail(@PathVariable Long id,
                                                     @RequestBody UpdateTaskDefinitionRequest request) {
        return RequestResult.success(taskDefinitionService.convertToVO(taskDefinitionService.update(id, request)));
    }

    @GetMapping("/task-definitions/{id}")
    @ApiOperation("Get TaskDefinition")
    public RequestResult<TaskDefinitionVO> getTaskDefinitionDetail(@PathVariable Long id) {
        TaskDefinition taskDefinition = taskDefinitionService.find(id);
        return RequestResult.success(taskDefinitionService.convertToVO(taskDefinition));
    }

    @DeleteMapping("/task-definitions/{id}")
    @ApiOperation("Delete TaskDefinition")
    public RequestResult<Object> deleteTaskDefinitionDetail(@PathVariable Long id) {
        taskDefinitionService.delete(id);
        return RequestResult.success();
    }

    /*--------- actions ----------*/

    @PostMapping("/task-definitions/{id}/_check")
    @ApiOperation("Check TaskDefinition current config is valid")
    public RequestResult<TaskTryVO> checkTaskDefinition(@PathVariable Long id) {
        TaskDefinition taskDefinition = taskDefinitionService.find(id);
        taskDefinitionService.checkRule(taskDefinition);
        return RequestResult.success();
    }

    @PostMapping("/task-definitions/{id}/_deploy")
    @ApiOperation("Commit and Deploy a TaskDefinition")
    public RequestResult<DeployVO> deployTaskDefinitionDirectly(@PathVariable Long id,
                                                                @RequestBody(required = false) CommitRequest request) {
        Deploy deploy = deployService.deployFast(id, request);
        return RequestResult.success(deployService.convertVO(deploy));
    }

    @GetMapping("/task-definitions/{taskDefId}/task-def-views")
    @ApiOperation("Fetch all task definition views that contain target task definition")
    public RequestResult<List<TaskDefinitionViewVO>> getTaskDefinitionViewsContainingTaskDefinition(@PathVariable Long taskDefId) {
        taskDefinitionService.find(taskDefId);   // throws NoSuchElementException if not found
        List<TaskDefinitionViewVO> viewVOList = taskDefinitionViewService.fetchAllByTaskDefinitionId(taskDefId);
        return RequestResult.success(viewVOList);
    }

    /*--------- task tries ----------*/

    @PostMapping("/task-definitions/{id}/_run")
    @ApiOperation("Try to run TaskDefinition")
    @OperationRecord(type = OperationRecordType.TASK_TRY_RUN, args = {"#id", "#taskRunRequest"})
    public RequestResult<TaskTryVO> runTaskDefinition(@PathVariable Long id,
                                                      @RequestBody TaskRunRequest taskRunRequest) {
        TaskTry taskTry = taskDefinitionService.run(id, taskRunRequest);
        return RequestResult.success(taskDefinitionService.convertToTaskTryVO(taskTry));
    }

    @PostMapping("/task-tries/{id}/_stop")
    @ApiOperation("Stop TaskTry")
    public RequestResult<TaskTryVO> stopTaskDefinitionDetail(@PathVariable Long id) {
        TaskTry taskTry = taskDefinitionService.stop(id);
        return RequestResult.success(taskDefinitionService.convertToTaskTryVO(taskTry));
    }

    @PostMapping("/task-tries/_runBatch")
    @ApiOperation("Try to run batch TaskDefinition")
    public RequestResult<List<TaskTryVO>> runTaskDefinitions(@RequestBody TaskTryBatchRequest taskTryBatchRequest) {
        Preconditions.checkArgument(taskTryBatchRequest != null, "TaskDefinition id list should not be null");
        List<TaskTry> taskTryList = taskDefinitionService.runBatch(taskTryBatchRequest);
        return RequestResult.success(taskDefinitionService.convertToTaskTryVOList(taskTryList));

    }

    @PostMapping("/task-tries/_stopBatch")
    @ApiOperation("Try to run batch TaskDefinition")
    public RequestResult<AcknowledgementVO> stopTaskDefinitions(@RequestBody TaskTryBatchRequest taskTryBatchRequest) {
        Preconditions.checkArgument(taskTryBatchRequest != null, "TaskTry id list should not be null");
        taskDefinitionService.stopBatch(taskTryBatchRequest);
        return RequestResult.success(new AcknowledgementVO("Operation acknowledged."));

    }

    @GetMapping("/task-tries/{id}")
    @ApiOperation("Get TaskTry")
    public RequestResult<TaskTryVO> getTaskTry(@PathVariable Long id) {
        TaskTry taskTry = taskDefinitionService.findTaskTry(id);
        return RequestResult.success(taskDefinitionService.convertToTaskTryVO(taskTry));
    }

    @GetMapping("/task-tries/{id}/log")
    @ApiOperation("Get TaskTry log")
    public RequestResult<TaskRunLogVO> getTaskTryLog(@PathVariable Long id,
                                                   @RequestParam(required = false) Optional<Integer> start,
                                                   @RequestParam(required = false) Optional<Integer> end
                                                   ) {
        TaskTry taskTry = taskDefinitionService.findTaskTry(id);
        TaskRunLogRequest.Builder request = TaskRunLogRequest.newBuilder()
                .withTaskRunId(taskTry.getWorkflowTaskRunId())
                .withAttempt(-1);

        start.ifPresent(request::withStartLine);
        end.ifPresent(request::withEndLine);
        return RequestResult.success(taskDefinitionService.runLog(request.build()));
    }

    @GetMapping("/task-tries/{taskDefId}/taskruns")
    public RequestResult<PaginationResult<TaskRun>> getTaskTryRuns(@PathVariable Long taskDefId,
                                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                                       @RequestParam(defaultValue = "25") int pageSize,
                                                                       @RequestParam(required = false) String status,
                                                                       @RequestParam(required = false)
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
                                                                       @RequestParam(required = false)
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime) {
        TaskTryRunSearchRequest taskTryRunSearchRequest = new TaskTryRunSearchRequest(
                pageSize,
                pageNum,
                Collections.singletonList(taskDefId),
                TaskRunStatus.resolve(status),
                startTime,
                endTime);
        return RequestResult.success(taskDefinitionService.searchTaskTryRuns(taskTryRunSearchRequest));
    }

    @GetMapping("/task-tries/taskruns/{taskRunId}/log")
    public RequestResult<TaskRunLogVO> getTaskTryRunLog(@PathVariable Long taskRunId,
                                                        @RequestParam(required = false) Integer start,
                                                        @RequestParam(required = false) Integer end,
                                                        @RequestParam(required = false) Integer attempt) {
        int attemptNum = Objects.nonNull(attempt) ? attempt : -1;
        return RequestResult.success(taskDefinitionService.getTaskTryRunLog(taskRunId, start, end, attemptNum));

    }

    @PostMapping("/task-tries/taskruns/{taskRunId}/abort")
    @OperationRecord(type = OperationRecordType.TASK_TRY_TASKRUN_ABORT, args = {"#taskRunId"})
    public RequestResult<TaskRun> stopTaskTryRun(@PathVariable Long taskRunId) {
        return RequestResult.success(taskDefinitionService.stopTaskTryRun(taskRunId));
    }

    @PostMapping("/task-tries/taskruns/{taskRunId}/restart")
    @OperationRecord(type = OperationRecordType.TASK_TRY_TASKRUN_RESTART, args = {"#taskRunId"})
    public RequestResult<TaskRun> restartTaskTryRun(@PathVariable Long taskRunId) {
        return RequestResult.success(taskDefinitionService.restartTaskTryRun(taskRunId));
    }
}
