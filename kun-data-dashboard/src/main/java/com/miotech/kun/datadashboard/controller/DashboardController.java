package com.miotech.kun.datadashboard.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadashboard.model.bo.*;
import com.miotech.kun.datadashboard.model.entity.*;
import com.miotech.kun.datadashboard.service.MetadataService;
import com.miotech.kun.datadashboard.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    WorkflowService workflowService;

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

    @GetMapping("/dashboard/data-development/metrics")
    public RequestResult<DataDevelopmentMetrics> getDataDevelopmentMetrics() {
        return RequestResult.success(workflowService.getDataDevelopmentMetrics());
    }

    @GetMapping("/dashboard/data-development/date-time-metrics")
    public RequestResult<DateTimeMetrics> getDateTimeMetrics(DateTimeMetricsRequest request) {
        return RequestResult.success(workflowService.getDateTimeMetrics(request));
    }

    @GetMapping("/dashboard/data-development/tasks")
    public RequestResult<DataDevelopmentTasks> getDataDevelopmentTasks(DataDevelopmentTasksRequest tasksRequest) {
        return RequestResult.success(workflowService.getDataDevelopmentTasks(tasksRequest));
    }
}
