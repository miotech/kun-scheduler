package com.miotech.kun.datadiscovery.controller;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.bo.LineageGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.LineageTasksRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.service.LineageAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
            lineageTaskList = lineageAppService.getLineageTasksByNeighbors(request);
        } else {
            lineageTaskList = lineageAppService.getLineageTasksByEdgeInfo(request);
        }
        lineageTasks.setTasks(lineageTaskList);
        return RequestResult.success(lineageTasks);
    }

}
