package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.datadiscovery.model.bo.LineageGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.LineageTasksRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.service.LineageAppService;
import com.miotech.kun.datadiscovery.service.MetadataService;
import com.miotech.kun.datadiscovery.service.RdmService;
import com.miotech.kun.workflow.client.LineageQueryDirection;
import com.miotech.kun.workflow.client.WorkflowApiException;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeTaskInfo;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.miotech.kun.datadiscovery.service.LineageAppService.LATEST_TASK_LIMIT;
import static com.miotech.kun.datadiscovery.testing.mockdata.MockWorkFlowClientDataFactory.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @program: kun
 * @description: testing
 * @author: zemin  huang
 * @create: 2022-01-24 09:20
 **/
public class LineageAppServiceTest extends DataDiscoveryTestBase {

    @Autowired
    LineageAppService lineageAppService;
    @MockBean
    MetadataService metadataService;
    @MockBean
    private RdmService rdmService;

    private LineageTasksRequest getLineageTasksRequest() {
        LineageQueryDirection lineageQueryDirection = LineageQueryDirection.UPSTREAM;
        LineageTasksRequest lineageTasksRequest = new LineageTasksRequest();
        lineageTasksRequest.setDatasetGid(1L);
        lineageTasksRequest.setDirection(lineageQueryDirection.name());
        lineageTasksRequest.setDestDatasetGid(1L);
        lineageTasksRequest.setSourceDatasetGid(1L);
        return lineageTasksRequest;
    }

    @Test
    void testGetLineageTasksByEdgeInfo_fetch_null() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        when(workflowClient.getLineageEdgeInfo(lineageTasksRequest.getSourceDatasetGid(), lineageTasksRequest.getDestDatasetGid()))
                .thenReturn(null);
        when(workflowClient.getLatestTaskRuns(mock(List.class), LATEST_TASK_LIMIT, false)).thenReturn(null);
        List<LineageTask> taskList = lineageAppService.getLineageTasksByEdgeInfo(lineageTasksRequest);
        Assertions.assertTrue(CollectionUtils.isEmpty(taskList));


    }

    @Test
    void testGetLineageTasksByEdgeInfo_fetch_entity() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();

        Long taskId = 1L;
        String taskName = "taskInfoOne";
        ArrayList<TaskRun> taskRunArrayList = Lists.newArrayList();
        TaskRun taskRun1 = getTaskRun(1L, "202201010000", TaskRunStatus.SUCCESS);
        TaskRun taskRun2 = getTaskRun(2L, "202201020000", TaskRunStatus.FAILED);
        TaskRun taskRun3 = getTaskRun(3L, "202201030000", TaskRunStatus.SUCCESS);
        taskRunArrayList.add(taskRun1);
        taskRunArrayList.add(taskRun2);
        taskRunArrayList.add(taskRun3);

        EdgeTaskInfo edgeTaskInfo = getEdgeTaskInfo(taskId, taskName);
        doReturn(createEdgeInfo(Lists.newArrayList(edgeTaskInfo)))
                .when(workflowClient).getLineageEdgeInfo(anyLong(), anyLong());
        doReturn(createLatestTaskRuns(taskId, taskRunArrayList))
                .when(workflowClient).getLatestTaskRuns(anyList(), anyInt(), anyBoolean());
        List<LineageTask> taskList = lineageAppService.getLineageTasksByEdgeInfo(lineageTasksRequest);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(taskList));
        assertThat(taskList.size(), is(1));
        LineageTask lineageTask = taskList.get(0);
        assertThat(lineageTask.getTaskId(), is(edgeTaskInfo.getId()));
        assertThat(lineageTask.getTaskName(), is(edgeTaskInfo.getName()));
        List<String> historyList = taskRunArrayList.stream().map(taskRun -> taskRun.getStatus().name()).collect(Collectors.toList());
        assertThat(lineageTask.getHistoryList(), is(historyList));
        assertThat(lineageTask.getLastExecutedTime(), is(taskRun3.getStartAt()));

    }


    @Test
    void testGetLineageTasksByNeighbors_fetch_null() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        when(workflowClient.getLineageNeighbors(lineageTasksRequest.getDatasetGid(),
                LineageQueryDirection.valueOf(lineageTasksRequest.getDirection()), 1))
                .thenReturn(null);
        when(workflowClient.getLatestTaskRuns(mock(List.class), LATEST_TASK_LIMIT, false)).thenReturn(null);
        List<LineageTask> taskList = lineageAppService.getLineageTasksByNeighbors(lineageTasksRequest);
        Assertions.assertTrue(CollectionUtils.isEmpty(taskList));
    }


    @Test
    void testGetLineageTasksByNeighbors_fetch_Upstream() {

        Long dataSetId = 1L;

        Task testDownTask = getTask(1L, "testDownTask");
        Task testUpTask = getTask(2L, "testUpTask");
        doReturn(createLineageNeighbors(Lists.newArrayList(testUpTask), Lists.newArrayList(testDownTask), dataSetId))
                .when(workflowClient).getLineageNeighbors(anyLong(), any(LineageQueryDirection.class), anyInt());
        ArrayList<TaskRun> taskRunArrayList = org.apache.commons.compress.utils.Lists.newArrayList();
        TaskRun taskRun1 = getTaskRun(1L, "202201010000", TaskRunStatus.SUCCESS);
        TaskRun taskRun2 = getTaskRun(2L, "202201020000", TaskRunStatus.FAILED);
        TaskRun taskRun3 = getTaskRun(3L, "202201030000", TaskRunStatus.SUCCESS);
        taskRunArrayList.add(taskRun1);
        taskRunArrayList.add(taskRun2);
        taskRunArrayList.add(taskRun3);
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        doReturn(createLatestTaskRuns(testUpTask.getId(), taskRunArrayList))
                .when(workflowClient).getLatestTaskRuns(anyList(), anyInt(), anyBoolean());
        List<LineageTask> taskList = lineageAppService.getLineageTasksByNeighbors(lineageTasksRequest);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(taskList));
        MatcherAssert.assertThat(taskList.size(), is(1));
        LineageTask lineageTask = taskList.get(0);
        assertThat(lineageTask.getTaskId(), is(testUpTask.getId()));
        assertThat(lineageTask.getTaskName(), is(testUpTask.getName()));
        List<String> historyList = taskRunArrayList.stream().map(taskRun -> taskRun.getStatus().name()).collect(Collectors.toList());
        assertThat(lineageTask.getHistoryList(), is(historyList));
        assertThat(lineageTask.getLastExecutedTime(), is(taskRun3.getStartAt()));
    }


    @Test
    void testGetLineageTasksByNeighbors_fetch_DownStream() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        lineageTasksRequest.setDirection(LineageQueryDirection.DOWNSTREAM.name());
        Task testDownTask = getTask(1L, "testDownTask");
        Task testUpTask = getTask(2L, "testUpTask");
        ArrayList<TaskRun> taskRunArrayList = org.apache.commons.compress.utils.Lists.newArrayList();
        TaskRun taskRun1 = getTaskRun(1L, "202201010000", TaskRunStatus.SUCCESS);
        TaskRun taskRun2 = getTaskRun(2L, "202201020000", TaskRunStatus.FAILED);
        TaskRun taskRun3 = getTaskRun(3L, "202201030000", TaskRunStatus.SUCCESS);
        taskRunArrayList.add(taskRun1);
        taskRunArrayList.add(taskRun2);
        taskRunArrayList.add(taskRun3);
        doReturn(createLineageNeighbors(Lists.newArrayList(testUpTask), Lists.newArrayList(testDownTask), lineageTasksRequest.getDatasetGid()))
                .when(workflowClient).getLineageNeighbors(anyLong(), any(LineageQueryDirection.class), anyInt());
        doReturn(createLatestTaskRuns(testDownTask.getId(), taskRunArrayList))
                .when(workflowClient).getLatestTaskRuns(anyList(), anyInt(), anyBoolean());
        List<LineageTask> taskList = lineageAppService.getLineageTasksByNeighbors(lineageTasksRequest);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(taskList));
        assertThat(taskList.size(), is(1));
        LineageTask lineageTask = taskList.get(0);
        assertThat(lineageTask.getTaskId(), is(testDownTask.getId()));
        assertThat(lineageTask.getTaskName(), is(testDownTask.getName()));
        List<String> historyList = taskRunArrayList.stream().map(taskRun -> taskRun.getStatus().name()).collect(Collectors.toList());
        assertThat(lineageTask.getHistoryList(), is(historyList));
    }


    @Test
    void testGetLineageGraph_fetch_entity_BOTH() {
        LineageGraphRequest request = new LineageGraphRequest();
        request.setDatasetGid(1L);
        request.setDepth(1);
        DatasetNodeInfo nodeInfo = getDatasetNodeInfo(1L, "testDataSetNameOne");
        DatasetNodeInfo upNodeInfo = getDatasetNodeInfo(2L, "testUpDataSetNameOne");
        DatasetNodeInfo downNodeInfo = getDatasetNodeInfo(3L, "testDownDataSetNameOne");
        ArrayList<DatasetNodeInfo> datasetNodeInfos = Lists.newArrayList(nodeInfo, upNodeInfo, downNodeInfo);

        doReturn(createLineageNeighborsGraph(nodeInfo,
                Lists.newArrayList(upNodeInfo), Lists.newArrayList(downNodeInfo)))
                .when(workflowClient).getLineageNeighbors(anyLong(), any(LineageQueryDirection.class), anyInt());
        doReturn(createLineageDatasetBasicList(datasetNodeInfos)).when(metadataService).getDatasetDetailList(anyList());
        LineageGraph lineageGraph = lineageAppService.getLineageGraph(request);
        assertThat(lineageGraph, is(notNullValue()));
        List<LineageVertex> vertices = lineageGraph.getVertices();
        assertThat(vertices, is(notNullValue()));
        assertThat(vertices.size(), is(3));
        LineageVertex lineageVertex = vertices.get(0);
        assertThat(lineageVertex.getVertexId(), is(nodeInfo.getGid()));
        LineageDatasetBasic datasetBasic = lineageVertex.getDatasetBasic();

        assertThat(datasetBasic, is(notNullValue()));
        assertThat(datasetBasic.getGid(), is(nodeInfo.getGid()));
        assertThat(datasetBasic.getName(), is(nodeInfo.getDatasetName()));
        List<LineageEdge> edges = lineageGraph.getEdges();
        assertThat(edges.size(), is(2));
        LineageEdge lineageEdgeUp = edges.get(0);

        assertThat(lineageEdgeUp.getSourceVertexId(), is(upNodeInfo.getGid()));
        assertThat(lineageEdgeUp.getDestVertexId(), is(nodeInfo.getGid()));
        LineageEdge lineageEdgeDown = edges.get(1);
        assertThat(lineageEdgeDown.getSourceVertexId(), is(nodeInfo.getGid()));
        assertThat(lineageEdgeDown.getDestVertexId(), is(downNodeInfo.getGid()));

    }

    @Test
    void test_get_downstream_dataset() {
        List<DatasetNodeInfo> downstreamDataset1 = lineageAppService.getDownstreamDataset(null);
        assertThat(downstreamDataset1, is(nullValue()));
        when(workflowClient.getLineageNeighbors(anyLong(), any(LineageQueryDirection.class), anyInt())).thenThrow(WorkflowApiException.class);
        List<DatasetNodeInfo> downstreamDataset2 = lineageAppService.getDownstreamDataset(2L);
        assertThat(downstreamDataset2.size(), is(0));
        Long datasetId = 1L;
        DatasetNodeInfo nodeInfo = getDatasetNodeInfo(datasetId, "testDataSetNameOne");
        DatasetNodeInfo upNodeInfo = getDatasetNodeInfo(2L, "testUpDataSetNameOne");
        DatasetNodeInfo downNodeInfo = getDatasetNodeInfo(3L, "testDownDataSetNameOne");
        ArrayList<DatasetNodeInfo> upList = Lists.newArrayList(upNodeInfo);
        ArrayList<DatasetNodeInfo> downList = Lists.newArrayList(downNodeInfo);
        doReturn(createLineageNeighborsGraph(nodeInfo, upList, downList))
                .when(workflowClient).getLineageNeighbors(anyLong(), any(LineageQueryDirection.class), anyInt());
        List<DatasetNodeInfo> downstreamDataset = lineageAppService.getDownstreamDataset(datasetId);
        assertThat(downstreamDataset.size(), is(downList.size()));
        DatasetNodeInfo datasetNodeInfo = downstreamDataset.get(0);
        assertThat(datasetNodeInfo, is(downNodeInfo));
    }
}
