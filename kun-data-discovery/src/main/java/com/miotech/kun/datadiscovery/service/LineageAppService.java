package com.miotech.kun.datadiscovery.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miotech.kun.common.utils.WorkflowUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadiscovery.constant.Constants;
import com.miotech.kun.datadiscovery.model.bo.LineageGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.LineageTasksRequest;
import com.miotech.kun.datadiscovery.model.entity.LineageEdge;
import com.miotech.kun.datadiscovery.model.entity.LineageGraph;
import com.miotech.kun.datadiscovery.model.entity.LineageTask;
import com.miotech.kun.datadiscovery.model.entity.LineageVertex;
import com.miotech.kun.workflow.client.LineageQueryDirection;
import com.miotech.kun.workflow.client.WorkflowApiException;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: lineage service
 * @author: zemin  huang
 * @create: 2022-01-24 09:23
 **/

@Service
@Slf4j
public class LineageAppService {

    public  static final int LATEST_TASK_LIMIT = 6;
    @Autowired
    WorkflowClient workflowClient;

    @Autowired
    DatasetService datasetService;


    public LineageGraph getLineageGraph(LineageGraphRequest request) {
        DatasetLineageInfo datasetLineageInfo = workflowClient.getLineageNeighbors(request.getDatasetGid(),
                LineageQueryDirection.valueOf(request.getDirection()),
                request.getDepth());
        LineageGraph lineageGraph = new LineageGraph();
        DatasetNodeInfo centerNode = datasetLineageInfo.getSourceNode();
        Map<Long, LineageVertex> vertexIdMap = new HashMap<>();
        LineageVertex centerVertex = new LineageVertex();
        centerVertex.setVertexId(centerNode.getGid());
        centerVertex.setUpstreamVertexCount(centerNode.getUpstreamDatasetCount());
        centerVertex.setDownstreamVertexCount(centerNode.getDownstreamDatasetCount());
        vertexIdMap.put(centerNode.getGid(), centerVertex);
        addUpstreamEdges(datasetLineageInfo, lineageGraph, centerNode, vertexIdMap);
        addDownstreamEdges(datasetLineageInfo, lineageGraph, centerNode, vertexIdMap);
        addVertices(lineageGraph, vertexIdMap);
        return lineageGraph;
    }

    private void addVertices(LineageGraph lineageGraph, Map<Long, LineageVertex> vertexIdMap) {
        List<LineageVertex> lineageVertices = datasetService.getDatasets(Lists.newArrayList(vertexIdMap.keySet())).stream()
                .map(datasetBasic -> {
                    LineageVertex vertex = vertexIdMap.get(datasetBasic.getGid());
                    vertex.setDatasetBasic(datasetBasic);
                    return vertex;
                }).collect(Collectors.toList());
        lineageGraph.setVertices(lineageVertices);
    }

    private void addDownstreamEdges(DatasetLineageInfo datasetLineageInfo, LineageGraph lineageGraph, DatasetNodeInfo centerNode, Map<Long, LineageVertex> vertexIdMap) {
        List<LineageEdge> downstreamEdges = datasetLineageInfo.getDownstreamNodes().stream()
                .map(datasetNodeInfo -> {
                    LineageEdge lineageEdge = new LineageEdge();
                    lineageEdge.setSourceVertexId(centerNode.getGid());
                    lineageEdge.setDestVertexId(datasetNodeInfo.getGid());
                    resolveDatasetNodeInfo(datasetNodeInfo, vertexIdMap);
                    return lineageEdge;
                })
                .collect(Collectors.toList());
        lineageGraph.addAll(downstreamEdges);
    }

    private void addUpstreamEdges(DatasetLineageInfo datasetLineageInfo,
                                  LineageGraph lineageGraph,
                                  DatasetNodeInfo centerNode,
                                  Map<Long, LineageVertex> vertexIdMap) {
        List<LineageEdge> upstreamEdges = datasetLineageInfo.getUpstreamNodes().stream()
                .map(datasetNodeInfo -> {
                    LineageEdge lineageEdge = new LineageEdge();
                    lineageEdge.setSourceVertexId(datasetNodeInfo.getGid());
                    lineageEdge.setDestVertexId(centerNode.getGid());
                    resolveDatasetNodeInfo(datasetNodeInfo, vertexIdMap);
                    return lineageEdge;
                })
                .collect(Collectors.toList());
        lineageGraph.addAll(upstreamEdges);
    }

    private void resolveDatasetNodeInfo(DatasetNodeInfo datasetNodeInfo,
                                        Map<Long, LineageVertex> vertexIdMap) {
        LineageVertex lineageVertex = new LineageVertex();
        lineageVertex.setVertexId(datasetNodeInfo.getGid());
        lineageVertex.setUpstreamVertexCount(datasetNodeInfo.getUpstreamDatasetCount());
        lineageVertex.setDownstreamVertexCount(datasetNodeInfo.getDownstreamDatasetCount());
        vertexIdMap.put(datasetNodeInfo.getGid(), lineageVertex);
    }


    public List<LineageTask> getLineageTasksResultBySourceDatasetGid(LineageTasksRequest request) {
        Long sourceDatasetGid = request.getSourceDatasetGid();
        Long destDatasetGid = request.getDestDatasetGid();
        Preconditions.checkNotNull(sourceDatasetGid, "Invalid argument `sourceDatasetGid`: null");
        Preconditions.checkNotNull(destDatasetGid, "Invalid argument `destDatasetGid`: null");
        EdgeInfo edgeInfo = workflowClient.getLineageEdgeInfo(sourceDatasetGid, destDatasetGid);
        if (Objects.isNull(edgeInfo) || CollectionUtils.isEmpty(edgeInfo.getTaskInfos())) {
            log.info("edge info maybe is null or task info is empty ");
            return Lists.newArrayList();
        }
        Map<Long, LineageTask> lineageTaskMap = edgeInfo
                .getTaskInfos()
                .stream()
                .map(edgeTaskInfo -> {
                    LineageTask lineageTask = new LineageTask();
                    lineageTask.setTaskId(edgeTaskInfo.getId());
                    lineageTask.setTaskName(edgeTaskInfo.getName());
                    return lineageTask;
                })
                .collect(Collectors.toMap(LineageTask::getTaskId, lineageTask -> lineageTask));

        if (CollectionUtils.isNotEmpty(lineageTaskMap.keySet())) {
            return Lists.newArrayList();
        }
        return getLineageTasks(lineageTaskMap);
    }

    private List<LineageTask> getLineageTasks(Map<Long, LineageTask> lineageTaskMap) {
        Map<Long, List<TaskRun>> latestTaskRunsMap = workflowClient.getLatestTaskRuns(new ArrayList<>(lineageTaskMap.keySet()), LATEST_TASK_LIMIT,false);
        lineageTaskMap.forEach((taskId, task) -> {
            List<TaskRun> latestTaskRuns = latestTaskRunsMap.get(taskId);
            if (CollectionUtils.isNotEmpty(latestTaskRuns)) {
                task.setLastExecutedTime(latestTaskRuns.get(0).getStartAt());
                List<String> latestStatus = WorkflowUtils.resolveTaskHistory(latestTaskRuns);
                task.setHistoryList(latestStatus);
            }
        });
        return Lists.newArrayList(lineageTaskMap.values());
    }

    public List<LineageTask> getLineageTasksResultByDatasetGid(LineageTasksRequest request) {
        Long datasetGid = request.getDatasetGid();
        Preconditions.checkNotNull(datasetGid, "Invalid argument `datasetGid`: null");
        Preconditions.checkNotNull(request.getDirection(), "Invalid argument `getDirection()`: null");
        LineageQueryDirection lineageQueryDirection = LineageQueryDirection.valueOf(request.getDirection());
        ArrayList<LineageTask> resultLineageTaskList = Lists.newArrayList();
        Map<Long, LineageTask> taskMap;
        switch (lineageQueryDirection) {
            case UPSTREAM:
                taskMap = getTaskMap(datasetGid, lineageQueryDirection, DatasetNodeInfo::getUpstreamTasks);
                break;
            case DOWNSTREAM:
                taskMap = getTaskMap(datasetGid, lineageQueryDirection, DatasetNodeInfo::getDownstreamTasks);
                break;
            default:
                throw ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported lineage task direction " + request.getDirection()));
        }

        if (org.springframework.util.CollectionUtils.isEmpty(taskMap)) {
            log.info(" task info is empty ");
            return resultLineageTaskList;
        }
        return getLineageTasks(taskMap);
    }

    private Map<Long, LineageTask> getTaskMap(Long datasetGid, LineageQueryDirection lineageQueryDirection, Function<DatasetNodeInfo, List<Task>> taskFunction) {
        DatasetLineageInfo datasetLineageInfo;
        try {
            datasetLineageInfo = workflowClient.getLineageNeighbors(datasetGid, lineageQueryDirection, 1);
        } catch (WorkflowApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException("error", e);
        }
        if (Objects.isNull(datasetLineageInfo)) {
            return Maps.newHashMap();
        }

        DatasetNodeInfo sourceNode = datasetLineageInfo.getSourceNode();
        assert sourceNode != null;
        List<Task> tasks = taskFunction.apply(sourceNode);
        if (CollectionUtils.isEmpty(tasks)) {
            return Maps.newHashMap();
        }
        return tasks
                .stream()
                .filter(Objects::nonNull)
                .filter(task -> Objects.isNull(task.getTags()) || !task.getTags().contains(Constants.TAG_TYPE_MANUAL_RUN))
                .map(task -> {
                    LineageTask lineageTask = new LineageTask();
                    lineageTask.setTaskId(task.getId());
                    lineageTask.setTaskName(task.getName());
                    return lineageTask;
                })
                .collect(Collectors.toMap(LineageTask::getTaskId, task -> task));

    }
}
