package com.miotech.kun.datadiscovery.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.common.utils.WorkflowUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadiscovery.constant.Constants;
import com.miotech.kun.datadiscovery.model.bo.LineageGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.LineageTasksRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.service.DatasetService;
import com.miotech.kun.datadiscovery.service.LineageAppService;
import com.miotech.kun.workflow.client.LineageQueryDirection;
import com.miotech.kun.workflow.client.WorkflowApiException;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@RestController
@RequestMapping("/kun/api/v1")
@Slf4j
public class LineageController {

    @Autowired
    private LineageAppService lineageAppService;


    @GetMapping("/lineage/graph")
    public RequestResult<LineageGraph> getLineageGraph(LineageGraphRequest request) {
        Preconditions.checkNotNull(request, "Invalid argument `LineageGraph`: null");
        try {
            LineageGraph lineageGraph = lineageAppService.getLineageGraph(request);
            return RequestResult.success(lineageGraph);
        } catch (Exception e) {
            log.error("getLineageGraph:",e);
            return RequestResult.success();
        }





    }

    @GetMapping("/lineage/tasks")
    public RequestResult<LineageTasks> getLineageTasks(LineageTasksRequest request) {
        Preconditions.checkNotNull(request, "Invalid argument `LineageTasks`: null");
        LineageTasks lineageTasks = new LineageTasks();
        List<LineageTask> lineageTaskList;
        if (Objects.nonNull(request.getDatasetGid())) {
            lineageTaskList = lineageAppService.getLineageTasksResultByDatasetGid(request);
        } else {
            lineageTaskList = lineageAppService.getLineageTasksResultBySourceDatasetGid(request);
        }
        lineageTasks.setTasks(lineageTaskList);
        return RequestResult.success(lineageTasks);
    }

}
