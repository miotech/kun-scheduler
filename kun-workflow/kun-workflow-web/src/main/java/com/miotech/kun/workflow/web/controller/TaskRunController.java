package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.*;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.taskrun.bo.TaskRunDailyStatisticInfo;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunStateVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunVO;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.commons.utils.ArgumentCheckUtils.parseBooleanQueryParameter;

@Singleton
public class TaskRunController {

    public static final int SAFE_PAGE_SIZE_UPPER_LIMIT = 100;

    @Inject
    private TaskRunService taskRunService;

    @RouteMapping(url = "/taskruns/{taskRunId}", method = "GET")
    public TaskRunVO getTaskRunDetail(@RouteVariable long taskRunId) {
        return taskRunService.getTaskRunDetail(taskRunId)
                .orElseThrow(() -> new EntityNotFoundException("TaskRun with id \"" + taskRunId + "\" not found"));
    }


    @RouteMapping(url = "/taskruns/{taskRunId}/status", method = "GET")
    public TaskRunStateVO getTaskRunStatus(@RouteVariable long taskRunId) {
        return taskRunService.getTaskStatus(taskRunId);
    }

    /**
     * @param taskRunId
     * @param attempt:  if attempt not specified, use -1 mark as latest
     * @param startLine
     * @param endLine
     * @return
     */
    @RouteMapping(url = "/taskruns/{taskRunId}/logs", method = "GET")
    public TaskRunLogVO getTaskRunLog(@RouteVariable long taskRunId,
                                      @QueryParameter(defaultValue = "-1") int attempt,
                                      @QueryParameter(defaultValue = "0") long startLine,
                                      @QueryParameter(defaultValue = VALUE_DEFAULT.MAX_LINES) long endLine) {
        return taskRunService.getTaskRunLog(taskRunId, attempt, startLine, endLine);
    }

    @RouteMapping(url = "/taskruns", method = "GET")
    public PaginationVO<TaskRunVO> getTaskRuns(
            @QueryParameter(defaultValue = "1") int pageNum,
            @QueryParameter(defaultValue = "100") int pageSize,
            @QueryParameter List<String> status,
            @QueryParameter List<Long> taskIds,
            @QueryParameter String dateFrom,
            @QueryParameter String dateTo,
            @QueryParameter(defaultValue = "id") String sortKey,
            @QueryParameter(defaultValue = "DESC") String sortOrder,
            @QueryParameter(defaultValue = "false") String includeStartedOnly,
            @QueryParameter List<String> scheduleTypes
    ) {
        TaskRunSearchFilter.Builder filterBuilder = TaskRunSearchFilter.newBuilder()
                .withPageNum(pageNum)
                .withPageSize(pageSize);

        buildFilter(filterBuilder, status, taskIds, dateFrom, dateTo, sortKey, sortOrder, includeStartedOnly, scheduleTypes);

        TaskRunSearchFilter filter = filterBuilder.build();
        return taskRunService.searchTaskRunVOs(filter);
    }

    @RouteMapping(url = "/taskruns/_count", method = "GET")
    public int getTaskRunCount(@QueryParameter List<String> status,
                               @QueryParameter List<Long> taskIds,
                               @QueryParameter String dateFrom,
                               @QueryParameter String dateTo,
                               @QueryParameter(defaultValue = "false") String includeStartedOnly,
                               @QueryParameter List<String> scheduleTypes

    ) {
        TaskRunSearchFilter.Builder filterBuilder = TaskRunSearchFilter.newBuilder();
        buildFilter(filterBuilder, status, taskIds, dateFrom, dateTo, null, null, includeStartedOnly, scheduleTypes);
        TaskRunSearchFilter filter = filterBuilder.build();
        return taskRunService.countTaskRunVOs(filter);
    }

    @RouteMapping(url = "/taskruns/_countByDay", method = "GET")
    public List<TaskRunDailyStatisticInfo> getCountTaskRunsByDay(
            @QueryParameter List<String> status,
            @QueryParameter List<Long> taskIds,
            @QueryParameter String dateFrom,
            @QueryParameter String dateTo,
            @QueryParameter(defaultValue = "false") String includeStartedOnly,
            @QueryParameter(defaultValue = "0") Integer offsetHours,
            @QueryParameter List<String> scheduleTypes

    ) {
        TaskRunSearchFilter.Builder filterBuilder = TaskRunSearchFilter.newBuilder();
        buildFilter(filterBuilder, status, taskIds, dateFrom, dateTo, null, null, includeStartedOnly, scheduleTypes);
        TaskRunSearchFilter filter = filterBuilder.build();
        return taskRunService.countTaskRunVOsByDate(filter, offsetHours);
    }

    private void buildFilter(
            TaskRunSearchFilter.Builder filterBuilder,
            List<String> status,
            List<Long> taskIds,
            String dateFrom,
            String dateTo,
            String sortKey,
            String sortOrder,
            String includeStartedOnly,
            List<String> scheduleTypes
    ) {
        if (status != null && !status.isEmpty()) {
            Set<TaskRunStatus> statusFilterSet = status.stream()
                    .map(TaskRunStatus::valueOf)
                    .collect(Collectors.toSet());
            filterBuilder.withStatus(statusFilterSet);
        }
        if (StringUtils.isNoneBlank(dateFrom)) {
            filterBuilder
                    .withDateFrom(DateTimeUtils.fromISODateTimeString(dateFrom));
        }
        if (StringUtils.isNoneBlank(dateFrom)) {
            filterBuilder
                    .withDateTo(DateTimeUtils.fromISODateTimeString(dateTo));
        }
        if (taskIds != null && !taskIds.isEmpty()) {
            filterBuilder
                    .withTaskIds(taskIds);
        }
        if (StringUtils.isNoneBlank(sortKey)) {
            filterBuilder.withSortKey(sortKey);
        }
        if (StringUtils.isNoneBlank(sortOrder)) {
            filterBuilder.withSortOrder(sortOrder);
        }
        if (scheduleTypes != null && !scheduleTypes.isEmpty()) {
            filterBuilder.withScheduleType(scheduleTypes);
        }
        filterBuilder.withIncludeStartedOnly(parseBooleanQueryParameter(includeStartedOnly));
    }

    @RouteMapping(url = "/taskruns/_search", method = "POST")
    public PaginationVO<TaskRunVO> searchTaskRuns(@RequestBody TaskRunSearchFilter requestFilter) {
        TaskRunSearchFilter filter = requestFilter.cloneBuilder()
                .withPageNum(Objects.nonNull(requestFilter.getPageNum()) && (requestFilter.getPageNum() > 0) ? requestFilter.getPageNum() : 1)
                .withPageSize(Objects.nonNull(requestFilter.getPageSize()) && (requestFilter.getPageSize() > 0) ? requestFilter.getPageSize() : 100)
                .withSortKey(Objects.nonNull(requestFilter.getSortKey()) ? requestFilter.getSortKey() : "id")
                .withSortOrder(Objects.nonNull(requestFilter.getSortOrder()) ? requestFilter.getSortOrder() : "DESC")
                .build();
        return taskRunService.searchTaskRunVOs(filter);
    }

    @RouteMapping(url = "/taskruns/_count", method = "POST")
    public int countTaskRuns(@RequestBody TaskRunSearchFilter requestFilter) {
        TaskRunSearchFilter filter = requestFilter.cloneBuilder().build();
        return taskRunService.countTaskRunVOs(filter);
    }

    @RouteMapping(url = "/taskruns/_countByDay", method = "POST")
    public List<TaskRunDailyStatisticInfo> countTaskRunsByDay(
            @RequestBody TaskRunSearchFilter requestFilter,
            @QueryParameter(defaultValue = "0") Integer offsetHours
    ) {
        TaskRunSearchFilter filter = requestFilter.cloneBuilder().build();
        return taskRunService.countTaskRunVOsByDate(filter, offsetHours);
    }

    @RouteMapping(url = "/taskruns/{taskRunId}/_abort", method = "PUT")
    public Boolean abortTaskRuns(@RouteVariable long taskRunId) {
        return taskRunService.abortTaskRun(taskRunId);
    }

    @RouteMapping(url = "/taskruns/{id}/neighbors", method = "GET")
    public Object getTaskRunNeighbors(@RouteVariable Long id,
                                      @QueryParameter(defaultValue = "1") int upstreamLevel,
                                      @QueryParameter(defaultValue = "1") int downstreamLevel
    ) {
        return taskRunService.getNeighbors(id, upstreamLevel, downstreamLevel);
    }

    @RouteMapping(url = "/taskruns/latest", method = "GET")
    public Object fetchLatestTaskRuns(@QueryParameter List<Long> taskIds, @QueryParameter Integer limit) {
        Preconditions.checkArgument(Objects.nonNull(taskIds) && (!taskIds.isEmpty()), "Should specify at least one task id.");
        Preconditions.checkArgument(Objects.nonNull(limit) && (limit > 0), "argument `limit` should be a positive integer.");

        int safeLimit = (limit <= SAFE_PAGE_SIZE_UPPER_LIMIT) ? limit : SAFE_PAGE_SIZE_UPPER_LIMIT;
        Map<Long, List<TaskRunVO>> taskRunsVO = taskRunService.fetchLatestTaskRuns(taskIds, safeLimit);
        return taskRunsVO;
    }

    @RouteMapping(url = "/taskruns/changePriority", method = "PUT")
    public Object changeTaskRunPriority(@QueryParameter long taskRunId, @QueryParameter int priority) {
        return taskRunService.changeTaskAttemptPriority(taskRunId, priority);
    }
}
