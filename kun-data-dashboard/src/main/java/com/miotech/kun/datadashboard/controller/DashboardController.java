package com.miotech.kun.datadashboard.controller;

import com.miotech.kun.common.model.PageInfo;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.datadashboard.model.bo.ColumnMetricsRequest;
import com.miotech.kun.datadashboard.model.bo.RowCountChangeRequest;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.constant.Constants;
import com.miotech.kun.datadashboard.model.entity.*;
import com.miotech.kun.datadashboard.service.MetadataService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunSearchRequest;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/9/15
 */
@RestController
@RequestMapping("/kun/api/v1")
public class DashboardController {

    @Autowired
    MetadataService metadataService;

    @Autowired
    WorkflowClient workflowClient;

    @GetMapping("/dashboard/metadata/metrics")
    public RequestResult<MetadataMetrics> getMetadataMetrics() {
        return RequestResult.success(metadataService.getMetadataMetrics());
    }

    @GetMapping("/dashboard/metadata/max-row-count-change")
    public RequestResult<DatasetRowCountChanges> getRowCountChange(RowCountChangeRequest rowCountChangeRequest) {
        return RequestResult.success(metadataService.getRowCountChange(rowCountChangeRequest));
    }

    @GetMapping("/dashboard/test-cases")
    public RequestResult<DataQualityCases> getTestCases(TestCasesRequest testCasesRequest) {
        return RequestResult.success(metadataService.getTestCases(testCasesRequest));
    }

    @GetMapping("/dashboard/metadata/column/metrics")
    public RequestResult<ColumnMetricsList> getColumnMetricsList(ColumnMetricsRequest columnMetricsRequest) {
        return RequestResult.success(metadataService.getColumnMetricsList(columnMetricsRequest));
    }

    private static final List<Tag> DATA_PLATFORM_FILTER_TAGS = new ArrayList<>();
    static {
        Tag projectTag = new Tag(Constants.DATA_PLATFORM_TAG_PROJECT_NAME, Constants.DATA_PLATFORM_TAG_PROJECT_VALUE);
        DATA_PLATFORM_FILTER_TAGS.add(projectTag);
    }
    
    @GetMapping("/dashboard/data-development/metrics")
    public RequestResult<DataDevelopmentMetrics> getDataDevelopmentMetrics() {
        TaskRunSearchRequest successRequest = TaskRunSearchRequest.newBuilder().
                withDateFrom(DateTimeUtils.now().minusDays(1))
                .withStatus(TaskRunStatus.SUCCESS)
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withPageSize(0)
                .build();
        long successCount = workflowClient.searchTaskRun(successRequest).getTotalCount();

        TaskRunSearchRequest failedRequest = TaskRunSearchRequest.newBuilder().
                withDateFrom(DateTimeUtils.now().minusDays(1))
                .withStatus(TaskRunStatus.FAILED)
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withPageSize(0)
                .build();
        long failedCount = workflowClient.searchTaskRun(failedRequest).getTotalCount();

        TaskRunSearchRequest runningRequest = TaskRunSearchRequest.newBuilder()
                .withStatus(TaskRunStatus.RUNNING)
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withPageSize(0)
                .build();
        long runningCount = workflowClient.searchTaskRun(runningRequest).getTotalCount();

        TaskRunSearchRequest totalRequest = TaskRunSearchRequest.newBuilder()
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withPageSize(0)
                .build();
        long totalCount = workflowClient.searchTaskRun(totalRequest).getTotalCount();

        DataDevelopmentMetrics metrics = new DataDevelopmentMetrics();
        metrics.setSuccessTaskCount(successCount);
        metrics.setFailedTaskCount(failedCount);
        metrics.setRunningTaskCount(runningCount);
        metrics.setTotalTaskCount(totalCount);
        return RequestResult.success(metrics);
    }

    @GetMapping("/dashboard/data-development/date-time-metrics")
    public RequestResult<DateTimeMetrics> getDateTimeMetrics() {
        DateTimeMetrics dateTimeMetrics = new DateTimeMetrics();
        OffsetDateTime currentTime = DateTimeUtils.now();
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
            TaskRunSearchRequest totalRequest = TaskRunSearchRequest.newBuilder()
                    .withDateFrom(startTime)
                    .withDateTo(endTime)
                    .withTags(DATA_PLATFORM_FILTER_TAGS)
                    .withPageSize(0)
                    .build();
            long totalCount = workflowClient.searchTaskRun(totalRequest).getTotalCount();
            DateTimeTaskCount taskCount = new DateTimeTaskCount();
            taskCount.setTaskCount(totalCount);
            taskCount.setTime(DateUtils.dateTimeToMillis(endTime));
            dateTimeMetrics.add(taskCount);
        }
        return RequestResult.success(dateTimeMetrics);
    }

    @GetMapping("/dashboard/data-development/tasks")
    public RequestResult<DataDevelopmentTasks> getDataDevelopmentTasks(PageInfo pageInfo) {
        TaskRunSearchRequest searchRequest = TaskRunSearchRequest.newBuilder()
                .withTags(DATA_PLATFORM_FILTER_TAGS)
                .withPageNum(pageInfo.getPageNumber())
                .withPageSize(pageInfo.getPageSize())
                .withIncludeStartedOnly(true)
                .withSortKey("startAt")
                .withSortOrder("DESC")
                .build();

        DataDevelopmentTasks dataDevelopmentTasks = new DataDevelopmentTasks();
        PaginationResult<TaskRun> taskRunResult = workflowClient.searchTaskRun(searchRequest);
        for (TaskRun taskRun : taskRunResult.getRecords()) {
            DataDevelopmentTask task = new DataDevelopmentTask();
            task.setTaskName(taskRun.getTask().getName());
            task.setTaskStatus(taskRun.getStatus().name());
            task.setStartTime(DateUtils.dateTimeToMillis(taskRun.getStartAt()));
            task.setEndTime(DateUtils.dateTimeToMillis(taskRun.getEndAt()));
            dataDevelopmentTasks.add(task);
        }
        dataDevelopmentTasks.setPageNumber(taskRunResult.getPageNum());
        dataDevelopmentTasks.setPageSize(taskRunResult.getPageSize());
        dataDevelopmentTasks.setTotalCount(taskRunResult.getTotalCount());
        return RequestResult.success(dataDevelopmentTasks);
    }
}
