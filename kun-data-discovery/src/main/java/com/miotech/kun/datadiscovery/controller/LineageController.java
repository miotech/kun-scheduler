package com.miotech.kun.datadiscovery.controller;

import com.google.common.collect.Lists;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.common.utils.WorkflowUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadiscovery.constant.Constants;
import com.miotech.kun.datadiscovery.model.bo.LineageGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.LineageTasksRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.service.DatasetService;
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

    private static final int LATEST_TASK_LIMIT = 6;

    @Autowired
    WorkflowClient workflowClient;

    @Autowired
    DatasetService datasetService;

    private void resolveDatasetNodeInfo(DatasetNodeInfo datasetNodeInfo,
                                        Map<Long, LineageVertex> vertexIdMap) {
        LineageVertex lineageVertex = new LineageVertex();
        lineageVertex.setVertexId(datasetNodeInfo.getGid());
        lineageVertex.setUpstreamVertexCount(datasetNodeInfo.getUpstreamDatasetCount());
        lineageVertex.setDownstreamVertexCount(datasetNodeInfo.getDownstreamDatasetCount());
        vertexIdMap.put(datasetNodeInfo.getGid(), lineageVertex);
    }

    @GetMapping("/lineage/graph")
    public RequestResult<LineageGraph> getLineageGraph(LineageGraphRequest request) {
        DatasetLineageInfo datasetLineageInfo;
        try {
           datasetLineageInfo = workflowClient.getLineageNeighbors(request.getDatasetGid(),
                    LineageQueryDirection.valueOf(request.getDirection()),
                    request.getDepth());
        } catch (WorkflowApiException e) {
            log.error(e.getMessage());
            return RequestResult.success();
        }
        LineageGraph lineageGraph = new LineageGraph();
        DatasetNodeInfo centerNode = datasetLineageInfo.getSourceNode();
        Map<Long, LineageVertex> vertexIdMap = new HashMap<>();
        LineageVertex centerVertex = new LineageVertex();
        centerVertex.setVertexId(centerNode.getGid());
        centerVertex.setUpstreamVertexCount(centerNode.getUpstreamDatasetCount());
        centerVertex.setDownstreamVertexCount(centerNode.getDownstreamDatasetCount());
        vertexIdMap.put(centerNode.getGid(), centerVertex);
        List<LineageEdge> upstreamEdges = datasetLineageInfo.getUpstreamNodes().stream()
                .map(datasetNodeInfo -> {
                    LineageEdge lineageEdge = new LineageEdge();
                    lineageEdge.setSourceVertexId(datasetNodeInfo.getGid());
                    lineageEdge.setDestVertexId(centerNode.getGid());
                    resolveDatasetNodeInfo(datasetNodeInfo, vertexIdMap);
                    return lineageEdge;
                })
                .collect(Collectors.toList());
        List<LineageEdge> downstreamEdges = datasetLineageInfo.getDownstreamNodes().stream()
                .map(datasetNodeInfo -> {
                    LineageEdge lineageEdge = new LineageEdge();
                    lineageEdge.setSourceVertexId(centerNode.getGid());
                    lineageEdge.setDestVertexId(datasetNodeInfo.getGid());
                    resolveDatasetNodeInfo(datasetNodeInfo, vertexIdMap);
                    return lineageEdge;
                })
                .collect(Collectors.toList());
        lineageGraph.addAll(upstreamEdges);
        lineageGraph.addAll(downstreamEdges);

        List<LineageVertex> lineageVertices = datasetService.getDatasets(Lists.newArrayList(vertexIdMap.keySet())).stream()
                .map(datasetBasic -> {
                    LineageVertex vertex = vertexIdMap.get(datasetBasic.getGid());
                    vertex.setDatasetBasic(datasetBasic);
                    return vertex;
                }).collect(Collectors.toList());
        lineageGraph.setVertices(lineageVertices);
        return RequestResult.success(lineageGraph);
    }

    @GetMapping("/lineage/tasks")
    public RequestResult<LineageTasks> getLineageTasks(LineageTasksRequest request) {
        if (IdUtils.isNotEmpty(request.getDatasetGid())) {
            LineageQueryDirection lineageQueryDirection = LineageQueryDirection.valueOf(request.getDirection());
            Map<Long, Task> taskIdMap = new HashMap<>();
            if (lineageQueryDirection == LineageQueryDirection.UPSTREAM) {

                try {
                    DatasetLineageInfo datasetLineageInfo = workflowClient.getLineageNeighbors(request.getDatasetGid(), LineageQueryDirection.UPSTREAM, 1);
                    List<Task> tasks = datasetLineageInfo.getSourceNode().getUpstreamTasks();
                    taskIdMap.putAll(tasks.stream().filter(task -> {
                        List<Tag> tags = task.getTags();
                        return tags == null || !tags.contains(Constants.TAG_TYPE_MANUAL_RUN);
                    }).collect(Collectors.toMap(Task::getId, task -> task)));
                } catch (WorkflowApiException e) {
                    log.error(e.getMessage());
                }

            } else if (lineageQueryDirection == LineageQueryDirection.DOWNSTREAM) {

                try {
                    DatasetLineageInfo datasetLineageInfo = workflowClient.getLineageNeighbors(request.getDatasetGid(), LineageQueryDirection.DOWNSTREAM, 1);
                    List<Task> tasks = datasetLineageInfo.getSourceNode().getDownstreamTasks();
                    taskIdMap.putAll(tasks.stream().filter(task -> {
                        List<Tag> tags = task.getTags();
                        return tags == null || !tags.contains(Constants.TAG_TYPE_MANUAL_RUN);
                    }).collect(Collectors.toMap(Task::getId, task -> task)));
                } catch (WorkflowApiException e) {
                    log.error(e.getMessage());
                }

            } else {
                throw ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported lineage task direction " + request.getDirection()));
            }
            LineageTasks lineageTasks = new LineageTasks();
            if (CollectionUtils.isEmpty(taskIdMap.keySet())) {
                return RequestResult.success(lineageTasks);
            }
            List<Long> taskIds = Lists.newArrayList(taskIdMap.keySet());
            Map<Long, List<TaskRun>> latestTaskRunsMap = workflowClient.getLatestTaskRuns(taskIds, LATEST_TASK_LIMIT);

            latestTaskRunsMap.forEach((taskId, taskRuns) -> {
                LineageTask lineageTask = new LineageTask();
                lineageTask.setTaskId(taskId);
                lineageTask.setTaskName(taskIdMap.get(taskId).getName());
                if (CollectionUtils.isNotEmpty(taskRuns)) {
                    lineageTask.setLastExecutedTime(DateUtils.dateTimeToMillis(taskRuns.get(0).getStartAt()));
                }
                lineageTask.setHistoryList(WorkflowUtils.resolveTaskHistory(taskRuns));
                lineageTasks.add(lineageTask);
            });
            return RequestResult.success(lineageTasks);
        }
        EdgeInfo edgeInfo = workflowClient.getLineageEdgeInfo(request.getSourceDatasetGid(), request.getDestDatasetGid());
        Map<Long, LineageTask> lineageTaskMap = edgeInfo.getTaskInfos().stream().map(edgeTaskInfo -> {
            LineageTask lineageTask = new LineageTask();
            lineageTask.setTaskId(edgeTaskInfo.getId());
            lineageTask.setTaskName(edgeTaskInfo.getName());
            return lineageTask;
        }).collect(Collectors.toMap(LineageTask::getTaskId, lineageTask -> lineageTask));
        if (CollectionUtils.isNotEmpty(lineageTaskMap.keySet())) {
            Map<Long, List<TaskRun>> latestTaskRunsMap = workflowClient.getLatestTaskRuns(new ArrayList<>(lineageTaskMap.keySet()), LATEST_TASK_LIMIT);
            lineageTaskMap.forEach((taskId, task) -> {
                List<TaskRun> latestTaskRuns = latestTaskRunsMap.get(taskId);
                if (CollectionUtils.isNotEmpty(latestTaskRuns)) {
                    task.setLastExecutedTime(DateUtils.dateTimeToMillis(latestTaskRuns.get(0).getStartAt()));
                }
                List<String> latestStatus = WorkflowUtils.resolveTaskHistory(latestTaskRunsMap.get(taskId));
                task.setHistoryList(latestStatus);
            });
        }
        LineageTasks lineageTasks = new LineageTasks();
        lineageTasks.setTasks(Lists.newArrayList(lineageTaskMap.values()));
        return RequestResult.success(lineageTasks);
    }
}
