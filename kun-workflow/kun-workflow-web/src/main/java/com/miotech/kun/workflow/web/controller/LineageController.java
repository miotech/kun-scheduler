package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.workflow.common.lineage.node.DatasetNode;
import com.miotech.kun.workflow.common.lineage.node.TaskNode;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class LineageController {
    @Inject
    private LineageService lineageService;

    @RouteMapping(url= "/lineages", method = "GET")
    public DatasetLineageInfo getLineageNeighbors(
            @QueryParameter Long datasetGid,
            @QueryParameter(defaultValue = "BOTH") String direction,
            @QueryParameter(defaultValue = "1") Integer depth
    ) {
        // Pre-check if parameters are valid
        Preconditions.checkArgument(Objects.nonNull(datasetGid), "dataset global id cannot be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(direction), "Illegal query parameter `direction`: {}", direction);
        Preconditions.checkArgument(Objects.nonNull(depth) && (depth > 0), "Illegal query parameter `depth`: {}", depth);

        Set<DatasetNode> upstreamNodes = new LinkedHashSet<>();
        Set<DatasetNode> downstreamNodes = new LinkedHashSet<>();
        Optional<DatasetNode> sourceNode = lineageService.fetchDatasetNodeById(datasetGid);
        switch (direction) {
            case "UPSTREAM":
                upstreamNodes.addAll(lineageService.fetchUpstreamDatasetNodes(datasetGid, depth));
                break;
            case "DOWNSTREAM":
                downstreamNodes.addAll(lineageService.fetchDownstreamDatasetNodes(datasetGid, depth));
                break;
            case "BOTH":
                downstreamNodes.addAll(lineageService.fetchDownstreamDatasetNodes(datasetGid, depth));
                upstreamNodes.addAll(lineageService.fetchUpstreamDatasetNodes(datasetGid, depth));
                break;
            default:
                throw new IllegalArgumentException(String.format("Illegal query parameter `direction`: %s", direction));
        }

        return DatasetLineageInfo.newBuilder()
                .withSourceNode(sourceNode.map(this::datasetNodeToInfo).orElse(null))
                .withUpstreamNodes(datasetNodesToInfoList(upstreamNodes))
                .withDownstreamNodes(datasetNodesToInfoList(downstreamNodes))
                .withQueryDepth(depth)
                .build();
    }

    private DatasetNodeInfo datasetNodeToInfo(DatasetNode datasetNode) {
        return DatasetNodeInfo.newBuilder()
                .withGid(datasetNode.getGid())
                .withDatasetName(datasetNode.getDatasetName())
                .withUpstreamTaskIds(datasetNode.getUpstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toList()))
                .withDownstreamTaskIds(datasetNode.getDownstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toList()))
                .build();
    }

    private List<DatasetNodeInfo> datasetNodesToInfoList(Set<DatasetNode> datasetNodes) {
        return datasetNodes.stream().map(node -> DatasetNodeInfo.newBuilder()
                .withGid(node.getGid())
                .withDatasetName(node.getDatasetName())
                .withUpstreamTaskIds(node.getUpstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toList()))
                .withDownstreamTaskIds(node.getDownstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toList()))
                .build()
        ).collect(Collectors.toList());
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
}
