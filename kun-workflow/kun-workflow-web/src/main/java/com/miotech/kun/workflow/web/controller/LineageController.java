package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.workflow.common.lineage.node.DatasetNode;
import com.miotech.kun.workflow.common.lineage.service.LineageService;

import java.util.*;

@Singleton
public class LineageController {
    @Inject
    private LineageService lineageService;

    @RouteMapping(url= "/lineages", method = "GET")
    public List<DatasetNode> getLineageNeighbors(
            @QueryParameter Long datasetGid,
            @QueryParameter(defaultValue = "BOTH") String direction,
            @QueryParameter(defaultValue = "1") Integer depth
    ) {
        // Pre-check if parameters are valid
        Preconditions.checkArgument(Objects.nonNull(datasetGid), "dataset global id cannot be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(direction), "Illegal query parameter `direction`: {}", direction);
        Preconditions.checkArgument(Objects.nonNull(depth) && (depth > 0), "Illegal query parameter `depth`: {}", depth);

        Set<DatasetNode> datasetNodes = new LinkedHashSet<>();
        switch (direction) {
            case "UPSTREAM":
                datasetNodes.addAll(lineageService.fetchUpstreamDatasetNodes(datasetGid));
                break;
            case "DOWNSTREAM":
                datasetNodes.addAll(lineageService.fetchDownstreamDatasetNodes(datasetGid));
                break;
            case "BOTH":
                datasetNodes.addAll(lineageService.fetchDownstreamDatasetNodes(datasetGid));
                datasetNodes.addAll(lineageService.fetchUpstreamDatasetNodes(datasetGid));
                break;
            default:
                throw new IllegalArgumentException(String.format("Illegal query parameter `direction`: %s", direction));
        }

        return new ArrayList<>(datasetNodes);
    }
}
