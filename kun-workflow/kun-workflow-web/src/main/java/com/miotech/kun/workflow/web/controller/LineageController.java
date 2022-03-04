package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.core.model.lineage.*;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetInfo;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetNode;
import com.miotech.kun.workflow.core.model.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.task.Task;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class LineageController {
    @Inject
    private LineageService lineageService;



    @RouteMapping(url = "/lineages", method = "GET")
    public Object getLineageNeighbors(
            @QueryParameter Long datasetGid,
            @QueryParameter(defaultValue = "BOTH") String direction,
            @QueryParameter(defaultValue = "1") Integer depth
    ) {
        // Pre-check if parameters are valid
        Preconditions.checkArgument(Objects.nonNull(datasetGid), "dataset global id cannot be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(direction), "Illegal query parameter `direction`: {}", direction);
        Preconditions.checkArgument(Objects.nonNull(depth) && (depth > 0), "Illegal query parameter `depth`: {}", depth);

        return lineageService.searchLineageInfo(datasetGid, depth, direction);
    }

    @RouteMapping(url = "/lineage/datasets/upstream-task", method = "POST")
    public List<UpstreamTaskInformation> getLineageTask(@RequestBody UpstreamTaskRequest upstreamTaskRequest) {
        return lineageService.fetchDirectUpstreamTask(upstreamTaskRequest.getDatasetGids());
    }

    @RouteMapping(url = "/lineages/edges", method = "GET")
    public EdgeInfo getEdgeInfoBetweenNodes(
            @QueryParameter Long upstreamDatasetGid,
            @QueryParameter Long downstreamDatasetGid
    ) {
        Preconditions.checkArgument(Objects.nonNull(upstreamDatasetGid), "upstream dataset gid cannot be null.");
        Preconditions.checkArgument(Objects.nonNull(downstreamDatasetGid), "downstream dataset gid cannot be null.");
        return lineageService.fetchEdgeInfo(upstreamDatasetGid, downstreamDatasetGid);
    }

    @RouteMapping(url = "/lineages/outlets", method = "GET")
    public TaskOutlets fetchOutletNodes(@QueryParameter Long taskId) {
        Set<DatasetNode> datasetNodes = lineageService.fetchOutletNodes(taskId);
        return new TaskOutlets(datasetNodes.stream().map(node ->
                new DatasetInfo(node.getGid(), node.getDatasetName())).collect(Collectors.toSet()));
    }

}
