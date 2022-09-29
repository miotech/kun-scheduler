package com.miotech.kun.openapi.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.commons.db.sql.SortOrder;
import com.miotech.kun.dataplatform.facade.backfill.Backfill;
import com.miotech.kun.dataplatform.web.common.backfill.vo.BackfillDetailVO;
import com.miotech.kun.dataplatform.web.common.backfill.vo.BackfillSearchParams;
import com.miotech.kun.dataplatform.web.common.deploy.vo.DeployVO;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.TaskDefinitionSearchRequest;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.openapi.model.request.*;
import com.miotech.kun.openapi.model.response.*;
import com.miotech.kun.openapi.service.ApiService;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunSearchRequest;
import com.miotech.kun.workflow.client.model.TaskRunWithDependencies;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/kun/open-api")
public class ApiController {

    private static final List<String> SCHEDULE_TYPE_FILTER = Lists.newArrayList("SCHEDULED");

    @Autowired
    private ApiService apiService;

    @PostMapping("/auth")
    public RequestResult<Object> authenticate(@RequestBody UserRequest request) {
        return RequestResult.success(apiService.authenticate(request));
    }

    /* task-view related methods */

    @PostMapping("/task-view/create")
    public RequestResult<TaskViewVO> createTaskView(@RequestBody TaskViewCreateRequest request,
                                                    @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.createTaskView(request, token));
    }

    @GetMapping("/task-view")
    public RequestResult<PageResult<TaskViewVO>> getTaskViewList(@RequestParam(required = false, defaultValue = "100") Integer pageSize,
                                                                 @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                                 @RequestParam(required = false) String keyword) {
        TaskDefinitionViewSearchParams searchParams = TaskDefinitionViewSearchParams.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .keyword(keyword)
                .build();
        return RequestResult.success(apiService.getTaskViewList(searchParams));
    }

    @GetMapping("/task-view/{taskViewId}")
    public RequestResult<TaskViewDetailVO> getTaskViewDetail(@PathVariable Long taskViewId) {
        return RequestResult.success(apiService.getTaskViewDetail(taskViewId));
    }

    /* task-template related methods */

    @GetMapping("/task-template")
    public RequestResult<List<TaskTemplateVO>> getTaskTemplateList() {
        return RequestResult.success(apiService.getTaskTemplateList());
    }

    /* task related methods */

    @GetMapping("/task")
    public RequestResult<PaginationResult<TaskVO>> getTaskList(@RequestParam(required = false) String taskName,
                                                               @RequestParam(required = false) String taskTemplateName,
                                                               @RequestParam(required = false) String owner,
                                                               @RequestParam(required = false) Long taskViewId,
                                                               @RequestParam(defaultValue = "100") Integer pageSize,
                                                               @RequestParam(defaultValue = "1") Integer pageNum) {
        TaskDefinitionSearchRequest searchRequest = new TaskDefinitionSearchRequest(pageSize,
                pageNum,
                taskName,
                taskTemplateName,
                ImmutableList.of(),
                owner == null? ImmutableList.of() : ImmutableList.of(owner),
                Optional.of(false),
                taskViewId == null? ImmutableList.of() : ImmutableList.of(taskViewId)
        );
        return RequestResult.success(apiService.searchTask(searchRequest));
    }

    @GetMapping("/task/{taskId}")
    public RequestResult<TaskVO> getTaskDetail(@PathVariable Long taskId) {
        return RequestResult.success(apiService.getTask(taskId));
    }

    @PostMapping("/task/create")
    public RequestResult<TaskVO> createTask(@RequestBody TaskCreateRequest request,
                                                      @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.createTask(request, token));
    }

    @PutMapping("/task/update")
    public RequestResult<TaskVO> updateTask(@RequestBody TaskUpdateRequest request,
                                                      @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.updateTask(request, token));
    }

    @PostMapping("/task/deploy")
    public RequestResult<DeployVO> deployTask(@RequestBody TaskCommitRequest request,
                                              @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.deployTask(request, token));
    }

    @GetMapping("/task/{taskId}/taskruns")
    public RequestResult<PaginationResult<TaskRun>> fetchTaskRunByTaskId(@PathVariable Long taskId,
                                                                         @RequestParam(required = false, defaultValue = "500") int pageSize,
                                                                         @RequestParam(required = false, defaultValue = "1") int pageNum) {
        return RequestResult.success(apiService.fetchTaskRunByTaskId(taskId, pageSize, pageNum));
    }

    @GetMapping("/task/{taskId}/dependencies")
    public RequestResult<TaskVOWithDependencies> fetchTaskWithDependencies(@PathVariable Long taskId,
                                                                    @RequestParam(required = false, defaultValue = "1") int upstreamLevel,
                                                                    @RequestParam(required = false, defaultValue = "1") int downstreamLevel) {
        Preconditions.checkNotNull(taskId, "task id should not be null");
        return RequestResult.success(apiService.fetchTaskWithDependencies(taskId, upstreamLevel, downstreamLevel));
    }

    /* taskrun related methods */

    @PostMapping("/taskrun/changePriority")
    public RequestResult<Object> changeTaskRunPriority(@RequestBody TaskRunPriorityChangeRequest request,
                                                       @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.changeTaskRunPriority(request, token));
    }

    @GetMapping("/taskrun")
    public RequestResult<PaginationResult<TaskRun>> fetchTaskRunList(@RequestParam(required = false, defaultValue = "1") int pageNum,
                                                                     @RequestParam(required = false, defaultValue = "500") int pageSize,
                                                                     @RequestParam(required = false) List<TaskRunStatus> status,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createAfter,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createBefore,
                                                                     @RequestParam(required = false, defaultValue = "createdAt") String sortKey,
                                                                     @RequestParam(required = false, defaultValue = "DESC") String sortOrder) {
        TaskRunSearchRequest.Builder taskRunSearchRequest = TaskRunSearchRequest.newBuilder()
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .withSortKey(sortKey)
                .withSortOrder(sortOrder);
        if (Objects.nonNull(status)) {
            taskRunSearchRequest.withStatus(new HashSet<>(status));
        }
        if (Objects.nonNull(createAfter)) {
            taskRunSearchRequest.withDateFrom(createAfter);
        }
        if (Objects.nonNull(createBefore)) {
            taskRunSearchRequest.withDateTo(createBefore);
        }
        return RequestResult.success(apiService.fetchTaskRunList(taskRunSearchRequest.build()));
    }

    @GetMapping("/taskrun/{taskRunId}")
    public RequestResult<TaskRun> fetchTaskRun(@PathVariable Long taskRunId) {
        Preconditions.checkNotNull(taskRunId, "task run id should not be null");
        return RequestResult.success(apiService.fetchTaskRun(taskRunId));
    }

    @PostMapping("/taskrun/{taskRunId}/abort")
    public RequestResult<TaskRun> abortTaskRun(@PathVariable Long taskRunId, @RequestHeader("Authorization") String token) {
        apiService.setUserByToken(token);
        Preconditions.checkNotNull(taskRunId, "task run id should not be null");
        return RequestResult.success(apiService.abortTaskRun(taskRunId));
    }

    @PostMapping("/taskrun/{taskRunId}/restart")
    public RequestResult<TaskRun> restartTaskRun(@PathVariable Long taskRunId, @RequestHeader("Authorization") String token) {
        apiService.setUserByToken(token);
        Preconditions.checkNotNull(taskRunId, "task run id should not be null");
        return RequestResult.success(apiService.restartTaskRun(taskRunId));
    }

    @PostMapping("/taskrun/{taskRunId}/skip")
    public RequestResult<TaskRun> skipTaskRun(@PathVariable Long taskRunId, @RequestHeader("Authorization") String token) {
        apiService.setUserByToken(token);
        Preconditions.checkNotNull(taskRunId, "task run id should not be null");
        return RequestResult.success(apiService.skipTaskRun(taskRunId));
    }

    @PostMapping("/taskrun/{taskRunId}/removeDependency")
    public RequestResult<Object> removeTaskRunDependency(@PathVariable Long taskRunId,
                                                          @RequestBody List<Long> upstreamTaskRunIds,
                                                          @RequestHeader("Authorization") String token) {
        apiService.setUserByToken(token);
        Preconditions.checkNotNull(taskRunId, "task run id should not be null");
        apiService.removeTaskRunDependency(taskRunId, upstreamTaskRunIds);
        return RequestResult.success("Operation acknowledged");
    }

    @PostMapping("/taskrun/bulkRestart")
    public RequestResult<Object> restartTaskRuns(@RequestBody List<Long> taskRunIds,
                                                 @RequestHeader("Authorization") String token) {
        apiService.setUserByToken(token);
        Preconditions.checkNotNull(taskRunIds, "task run ids should not be null");
        apiService.restartTaskRuns(taskRunIds);
        return RequestResult.success(new AcknowledgementVO("Operation acknowledged"));
    }

    @GetMapping("/taskrun/{taskRunId}/dependencies")
    public RequestResult<TaskRunWithDependencies> fetchTaskRunWithDependencies(@PathVariable Long taskRunId,
                                                                               @RequestParam(required = false, defaultValue = "0") int upstreamLevel,
                                                                               @RequestParam(required = false, defaultValue = "0") int downstreamLevel) {
        Preconditions.checkNotNull(taskRunId, "task run id should not be null");
        return RequestResult.success(apiService.fetchTaskRunWithDependencies(taskRunId, upstreamLevel, downstreamLevel));
    }

    /* backfill related methods*/

    @GetMapping("/backfill")
    public RequestResult<PageResult<Backfill>> fetchBackfillList(@RequestParam(required = false, defaultValue = "1") int pageNum,
                                                     @RequestParam(required = false, defaultValue = "100") int pageSize,
                                                     @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
                                                     @RequestParam(required = false, defaultValue = "id") String sortKey) {
        BackfillSearchParams searchParams = new BackfillSearchParams();
        searchParams.setPageNumber(pageNum);
        searchParams.setPageSize(pageSize);
        searchParams.setSortKey(sortKey);
        searchParams.setSortOrder(SortOrder.from(sortOrder));
        return RequestResult.success(apiService.fetchBackfillList(searchParams));
    }

    @GetMapping("/backfill/{backfillId}")
    public RequestResult<BackfillDetailVO> fetchBackfillDetail(@PathVariable Long backfillId) {
        Preconditions.checkArgument(Objects.nonNull(backfillId), "backfill id cannot be null");
        return RequestResult.success(apiService.fetchBackfillDetail(backfillId));
    }

}
