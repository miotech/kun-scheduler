package com.miotech.kun.datadashboard.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.commons.utils.NumberUtils;
import com.miotech.kun.datadashboard.model.bo.DataDevelopmentTasksRequest;
import com.miotech.kun.datadashboard.model.bo.DateTimeMetricsRequest;
import com.miotech.kun.datadashboard.model.bo.StatisticChartRequest;
import com.miotech.kun.datadashboard.model.bo.StatisticChartTasksRequest;
import com.miotech.kun.datadashboard.model.constant.Constants;
import com.miotech.kun.datadashboard.model.entity.*;
import com.miotech.kun.datadashboard.model.entity.datadevelopment.DailyStatistic;
import com.miotech.kun.datadashboard.model.entity.datadevelopment.StatisticChartResult;
import com.miotech.kun.datadashboard.model.entity.datadevelopment.TaskResult;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunSearchRequest;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service("DashboardWorkflowService")
public class WorkflowService {

    private static final List<Tag> DATA_PLATFORM_FILTER_TAGS =
            Lists.newArrayList(
                    new Tag(Constants.DATA_PLATFORM_TAG_PROJECT_NAME, Constants.DATA_PLATFORM_TAG_PROJECT_VALUE)
            );
    private final ConcurrentMap<OffsetDateTime, DateTimeTaskCount> dateTimeTaskCountMap = new ConcurrentHashMap<>();

    private static final List<String> SCHEDULE_TYPE_FILTER = Lists.newArrayList("SCHEDULED");

    // we set 9 am as the start of a day in task statistic
    private final Integer RESET_HOUR = 9;
    private final Integer STATISTIC_DAYS = 8;

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

        TaskRunSearchRequest pendingRequest = TaskRunSearchRequest.newBuilder()
                .withDateFrom(DateTimeUtils.now().minusDays(1))
                .withIncludeStartedOnly(false)
                .withStatus(ImmutableSet.of(TaskRunStatus.CREATED, TaskRunStatus.QUEUED, TaskRunStatus.BLOCKED))
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer pendingCount = workflowClient.countTaskRun(pendingRequest);

        TaskRunSearchRequest upstreamFailedRequest = TaskRunSearchRequest.newBuilder()
                .withDateFrom(DateTimeUtils.now().minusDays(1))
                .withIncludeStartedOnly(false)
                .withStatus(ImmutableSet.of(TaskRunStatus.UPSTREAM_FAILED))
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageSize(0)
                .build();
        Integer upstreamFailedTaskCount = workflowClient.countTaskRun(upstreamFailedRequest);

        DataDevelopmentMetrics metrics = new DataDevelopmentMetrics();
        metrics.setSuccessTaskCount(successCount);
        metrics.setFailedTaskCount(failedCount);
        metrics.setRunningTaskCount(runningCount);
        metrics.setPendingTaskCount(pendingCount);
        metrics.setUpstreamFailedTaskCount(upstreamFailedTaskCount);

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
            taskCount.setTime(OffsetDateTime.ofInstant(endTime.toInstant(), ZoneOffset.ofHours(0)));
            dateTimeMetrics.add(taskCount);
            dateTimeTaskCountMap.put(startTime, taskCount);
        }
        return dateTimeMetrics;
    }

    public StatisticChartResult getStatisticChartResult(StatisticChartRequest statisticChartRequest) {
        Integer timezoneOffset = statisticChartRequest.getTimezoneOffset();
        OffsetDateTime currentTime = DateUtils.getCurrentDateTime(timezoneOffset);
        LocalDate currentDate = currentTime.toLocalDate();

        OffsetDateTime breakpoint = OffsetDateTime.of(currentDate, LocalTime.of(RESET_HOUR, 0), ZoneOffset.ofHours(timezoneOffset));
        //find previous reset hour
        OffsetDateTime start;
        OffsetDateTime end;
        if (currentTime.isAfter(breakpoint)) {
            start = breakpoint;
        } else {
            start = breakpoint.minusDays(1);
        }
        end = currentTime;

        List<DailyStatistic> dailyStatisticList = new ArrayList<>();

        //record 8 days statistic (1 current day and 7 full day)
        for (int i = 0; i < STATISTIC_DAYS; i++) {
            List<TaskResult> taskResultList = new ArrayList<>();
            TaskRunSearchRequest request;
            Integer totalCount = 0;
            Integer count;
            //count finished tasks at reset hour
            List<TaskRunStatus> terminatedStatus = ImmutableList.of(TaskRunStatus.SUCCESS, TaskRunStatus.FAILED,
                    TaskRunStatus.UPSTREAM_FAILED, TaskRunStatus.ABORTED);
            for (TaskRunStatus status : terminatedStatus) {
                request = buildTaskRunSearchRequest(start, end, ImmutableSet.of(status));
                count = workflowClient.countTaskRun(request);
                totalCount += count;
                taskResultList.add(new TaskResult(status.name(), status, count));
            }

            //count tasks unfinished at reset hour with current status
            List<TaskRunStatus> finalStatus = ImmutableList.of(TaskRunStatus.FAILED, TaskRunStatus.UPSTREAM_FAILED,
                    TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, TaskRunStatus.ABORTED, TaskRunStatus.CREATED, TaskRunStatus.BLOCKED);

            for (TaskRunStatus status : finalStatus) {
                request = buildTaskRunSearchRequest(start, end, ImmutableSet.of(status), end);
                count = workflowClient.countTaskRun(request);
                totalCount += count;
                taskResultList.add(new TaskResult("ONGOING", status, count));
            }

            DailyStatistic dailyStatistic = new DailyStatistic(OffsetDateTime.ofInstant(start.toInstant(), ZoneOffset.ofHours(0)), totalCount, taskResultList);
            dailyStatisticList.add(dailyStatistic);

            end = start;
            start = start.minusDays(1);
        }
        Collections.reverse(dailyStatisticList);
        return new StatisticChartResult(dailyStatisticList);
    }

    private TaskRunSearchRequest buildTaskRunSearchRequest(OffsetDateTime startOfCreation, OffsetDateTime endOfCreation) {
        return buildTaskRunSearchRequest(startOfCreation, endOfCreation, null, null);
    }

    private TaskRunSearchRequest buildTaskRunSearchRequest(OffsetDateTime startOfCreation, OffsetDateTime endOfCreation,
                                                           @Nullable Set<TaskRunStatus> status) {
        return buildTaskRunSearchRequest(startOfCreation, endOfCreation, status, null);
    }

    private TaskRunSearchRequest buildTaskRunSearchRequest(OffsetDateTime startOfCreation, OffsetDateTime endOfCreation,
                                                           @Nullable Set<TaskRunStatus> status, @Nullable OffsetDateTime startOfTermination) {
        TaskRunSearchRequest.Builder builder =  TaskRunSearchRequest.newBuilder()
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withDateFrom(startOfCreation)
                .withDateTo(endOfCreation);

        if (Objects.nonNull(status)) {
            builder.withStatus(status);
        }

        if (Objects.nonNull(startOfTermination)) {
            builder.withEndAfter(startOfTermination);
        }

        return builder.build();
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
        return searchTaskRuns(searchRequest);
    }

    public DataDevelopmentTasks getDataDevelopmentTasks(StatisticChartTasksRequest tasksRequest) {
        Integer timezoneOffset = tasksRequest.getTimezoneOffset();
        OffsetDateTime targetTime = DateUtils.millisToOffsetDateTime(tasksRequest.getTargetTime().toInstant().toEpochMilli(), ZoneOffset.ofHours(timezoneOffset));
        String status = tasksRequest.getStatus();
        TaskRunStatus finalStatus = tasksRequest.getFinalStatus();
        TaskRunSearchRequest.Builder builder = TaskRunSearchRequest.newBuilder()
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withPageNum(tasksRequest.getPageNumber())
                .withPageSize(tasksRequest.getPageSize())
                .withStatus(ImmutableSet.of(finalStatus))
                .withDateFrom(targetTime)
                .withDateTo(targetTime.plusDays(1))
                .withSortKey("createdAt")
                .withSortOrder("DESC");
        if (status.equals("ONGOING")) {
            builder.withEndAfter(targetTime.plusDays(1));
        }
        TaskRunSearchRequest searchRequest = builder.build();

        return searchTaskRuns(searchRequest);
    }

    private DataDevelopmentTasks searchTaskRuns(TaskRunSearchRequest searchRequest) {
        DataDevelopmentTasks dataDevelopmentTasks = new DataDevelopmentTasks();
        PaginationResult<TaskRun> taskRunResult = workflowClient.searchTaskRun(searchRequest);
        for (TaskRun taskRun : taskRunResult.getRecords()) {
            DataDevelopmentTask task = new DataDevelopmentTask();
            task.setTaskId(taskRun.getTask().getId());
            task.setTaskRunId(taskRun.getId());
            task.setTaskName(taskRun.getTask().getName());
            task.setTaskStatus(taskRun.getStatus().name());
            task.setStartTime(taskRun.getStartAt());
            task.setEndTime(taskRun.getEndAt());
            task.setCreateTime(taskRun.getCreatedAt());
            task.setUpdateTime(taskRun.getUpdatedAt());
            dataDevelopmentTasks.add(task);
        }
        dataDevelopmentTasks.setPageNumber(taskRunResult.getPageNum());
        dataDevelopmentTasks.setPageSize(taskRunResult.getPageSize());
        dataDevelopmentTasks.setTotalCount(taskRunResult.getTotalCount());
        return dataDevelopmentTasks;
    }
}
