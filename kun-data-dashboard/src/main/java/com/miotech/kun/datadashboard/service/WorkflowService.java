package com.miotech.kun.datadashboard.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.datadashboard.model.bo.DataDevelopmentTasksRequest;
import com.miotech.kun.datadashboard.model.bo.DateTimeMetricsRequest;
import com.miotech.kun.datadashboard.model.constant.Constants;
import com.miotech.kun.datadashboard.model.entity.*;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunSearchRequest;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class WorkflowService {

    private static final List<Tag> DATA_PLATFORM_FILTER_TAGS =
            Lists.newArrayList(
                    new Tag(Constants.DATA_PLATFORM_TAG_PROJECT_NAME, Constants.DATA_PLATFORM_TAG_PROJECT_VALUE),
                    new Tag(Constants.DATA_PLATFORM_TAG_KEY_TYPE, Constants.DATA_PLATFORM_TAG_VALUE_SCHEDULED)
            );
    private final ConcurrentMap<OffsetDateTime, DateTimeTaskCount> dateTimeTaskCountMap = new ConcurrentHashMap<>();

    private static final List<String> SCHEDULE_TYPE_FILTER = Lists.newArrayList("SCHEDULED");

    @Autowired
    WorkflowClient workflowClient;

    public DataDevelopmentMetrics getDataDevelopmentMetrics() {
        TaskRunSearchRequest successRequest = TaskRunSearchRequest.newBuilder().
                withDateFrom(DateTimeUtils.now().minusDays(1))
                .withStatus(Sets.newHashSet(TaskRunStatus.SUCCESS))
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer successCount = workflowClient.countTaskRun(successRequest);

        TaskRunSearchRequest failedRequest = TaskRunSearchRequest.newBuilder().
                withDateFrom(DateTimeUtils.now().minusDays(1))
                .withStatus(Sets.newHashSet(TaskRunStatus.FAILED, TaskRunStatus.ERROR))
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer failedCount = workflowClient.countTaskRun(failedRequest);

        TaskRunSearchRequest runningRequest = TaskRunSearchRequest.newBuilder()
                .withStatus(Sets.newHashSet(TaskRunStatus.RUNNING))
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer runningCount = workflowClient.countTaskRun(runningRequest);

        TaskRunSearchRequest startedRequest = TaskRunSearchRequest.newBuilder()
                .withIncludeStartedOnly(true)
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer startedCount = workflowClient.countTaskRun(startedRequest);

        TaskRunSearchRequest pendingRequest = TaskRunSearchRequest.newBuilder()
                .withDateFrom(DateTimeUtils.now().minusDays(1))
                .withIncludeStartedOnly(false)
                .withStatus(ImmutableSet.of(TaskRunStatus.CREATED, TaskRunStatus.INITIALIZING, TaskRunStatus.QUEUED, TaskRunStatus.UPSTREAM_FAILED))
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer pendingCount = workflowClient.countTaskRun(pendingRequest);

        TaskRunSearchRequest totalRequest = TaskRunSearchRequest.newBuilder()
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer totalCount = workflowClient.countTaskRun(totalRequest);

        DataDevelopmentMetrics metrics = new DataDevelopmentMetrics();
        metrics.setSuccessTaskCount(successCount);
        metrics.setFailedTaskCount(failedCount);
        metrics.setRunningTaskCount(runningCount);
        metrics.setStartedTaskCount(startedCount);
        metrics.setPendingTaskCount(pendingCount);
        metrics.setTotalTaskCount(totalCount);

        return metrics;
    }

    public DateTimeMetrics getDateTimeMetrics(DateTimeMetricsRequest request) {
        DateTimeMetrics dateTimeMetrics = new DateTimeMetrics();
        OffsetDateTime currentTime = DateUtils.getCurrentDateTime(request.getHours());
        int dayOfMonth = currentTime.getDayOfMonth();
        for (int i = 1; i <= dayOfMonth; i++) {
            OffsetDateTime computeTime = currentTime.minusDays(dayOfMonth - i);
            OffsetDateTime startTime = computeTime.with(LocalTime.MIN);
            OffsetDateTime endTime;
            if (i == dayOfMonth) {
                endTime = currentTime;
            } else {
                endTime = computeTime.with(LocalTime.MAX);
            }
            if (dateTimeTaskCountMap.get(startTime) != null && i != dayOfMonth) {
                dateTimeMetrics.add(dateTimeTaskCountMap.get(startTime));
                continue;
            }
            TaskRunSearchRequest totalRequest = TaskRunSearchRequest.newBuilder()
                    .withDateFrom(startTime)
                    .withDateTo(endTime)
                    .withTags(DATA_PLATFORM_FILTER_TAGS)
                    .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                    .withPageSize(0)
                    .build();
            Integer totalCount = workflowClient.countTaskRun(totalRequest);
            DateTimeTaskCount taskCount = new DateTimeTaskCount();
            taskCount.setTaskCount(totalCount);
            taskCount.setTime(DateUtils.dateTimeToMillis(endTime));
            dateTimeMetrics.add(taskCount);
            dateTimeTaskCountMap.put(startTime, taskCount);
        }
        return dateTimeMetrics;
    }

    public DataDevelopmentTasks getDataDevelopmentTasks(DataDevelopmentTasksRequest tasksRequest) {
        TaskRunSearchRequest searchRequest = TaskRunSearchRequest.newBuilder()
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withPageNum(tasksRequest.getPageNumber())
                .withPageSize(tasksRequest.getPageSize())
                .withStatus(Objects.nonNull(tasksRequest.getTaskRunStatus()) ?
                        Sets.newHashSet(tasksRequest.getTaskRunStatus()) : null)
                .withIncludeStartedOnly(tasksRequest.getIncludeStartedOnly())
                .withDateFrom(Objects.equals(tasksRequest.getLast24HoursOnly(), true) ? DateTimeUtils.now().minusHours(24) : null)
                .withSortKey("createdAt")
                .withSortOrder("DESC")
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .build();

        DataDevelopmentTasks dataDevelopmentTasks = new DataDevelopmentTasks();
        PaginationResult<TaskRun> taskRunResult = workflowClient.searchTaskRun(searchRequest);
        for (TaskRun taskRun : taskRunResult.getRecords()) {
            DataDevelopmentTask task = new DataDevelopmentTask();
            task.setTaskId(taskRun.getTask().getId());
            task.setTaskRunId(taskRun.getId());
            task.setTaskName(taskRun.getTask().getName());
            task.setTaskStatus(taskRun.getStatus().name());
            task.setStartTime(DateUtils.dateTimeToMillis(taskRun.getStartAt()));
            task.setEndTime(DateUtils.dateTimeToMillis(taskRun.getEndAt()));
            task.setCreateTime(DateUtils.dateTimeToMillis(taskRun.getCreatedAt()));
            task.setUpdateTime(DateUtils.dateTimeToMillis(taskRun.getUpdatedAt()));
            dataDevelopmentTasks.add(task);
        }
        dataDevelopmentTasks.setPageNumber(taskRunResult.getPageNum());
        dataDevelopmentTasks.setPageSize(taskRunResult.getPageSize());
        dataDevelopmentTasks.setTotalCount(taskRunResult.getTotalCount());
        return dataDevelopmentTasks;
    }
}
