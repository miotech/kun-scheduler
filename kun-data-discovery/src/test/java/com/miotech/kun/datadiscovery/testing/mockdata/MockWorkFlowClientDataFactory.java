package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.datadiscovery.model.entity.LineageDatasetBasic;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeTaskInfo;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.apache.commons.compress.utils.Lists;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: kun
 * @description: return  workflowclient mock data
 * @author: zemin  huang
 * @create: 2022-01-26 17:12
 **/
public class MockWorkFlowClientDataFactory {


    public static EdgeInfo createEdgeInfo() {
        ArrayList<EdgeTaskInfo> taskInfos = Lists.newArrayList();
        EdgeTaskInfo taskInfo_1 = EdgeTaskInfo.newBuilder()
                .withId(1L)
                .withName("taskInfoOne")
                .build();
        taskInfos.add(taskInfo_1);
        EdgeTaskInfo taskInfo_2 = EdgeTaskInfo.newBuilder()
                .withId(2L)
                .withName("taskInfoTwo")
                .build();
        taskInfos.add(taskInfo_2);
        EdgeInfo edgeInfo = EdgeInfo.newBuilder()
                .withTaskInfos(taskInfos)
                .withUpstreamDatasetGid(1L)
                .withDownstreamDatasetGid(1L)
                .build();
        return edgeInfo;
    }

    public static Map<Long, List<TaskRun>>  createLatestTaskRuns() {
        Map<Long, List<TaskRun>> map=new HashMap<>();
        ArrayList<TaskRun> taskRunArrayList = Lists.newArrayList();
        TaskRun taskRun1 = TaskRun.newBuilder().withId(1L).withCreatedAt(OffsetDateTime.now()).withStatus(TaskRunStatus.SUCCESS).build();
        TaskRun taskRun2 = TaskRun.newBuilder().withId(2L).withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun3 = TaskRun.newBuilder().withId(3L).withStatus(TaskRunStatus.SUCCESS).build();
        taskRunArrayList.add(taskRun1);
        taskRunArrayList.add(taskRun2);
        taskRunArrayList.add(taskRun3);
        map.put(1L,taskRunArrayList);
        map.put(2L,taskRunArrayList);

        return map;
    }

    public static DatasetLineageInfo createLineageNeighborsGraph() {
        List<DatasetNodeInfo> up=new ArrayList<>();

        List<DatasetNodeInfo> down=new ArrayList<>();
        DatasetNodeInfo nodeInfo = DatasetNodeInfo
                .newBuilder()
                .withGid(1L)
                .withDatasetName("testDataSetNameOne")
                .withDownstreamDatasetCount(1)
                .withDownstreamDatasetCount(1)
                .build();
        up.add(nodeInfo);
        down.add(nodeInfo);
        DatasetLineageInfo lineageInfo = DatasetLineageInfo.newBuilder()
                .withSourceNode(nodeInfo)
                .withUpstreamNodes(up).withDownstreamNodes(down).build();
        return lineageInfo;
    }
    public static DatasetLineageInfo createLineageNeighbors() {
        ArrayList<Task> downTask = Lists.newArrayList();
        Task testDownTask = Task.newBuilder().withId(1L).withName("testDownTask").withDependencies(Lists.newArrayList()).build();
        downTask.add(testDownTask);
        ArrayList<Task> upTask = Lists.newArrayList();
        Task testUpTask = Task.newBuilder().withId(2L).withName("testUpTask").withDependencies(Lists.newArrayList()).build();
        upTask.add(testUpTask);
        DatasetNodeInfo nodeInfo = DatasetNodeInfo
                .newBuilder()
                .withGid(1L)
                .withDatasetName("testDataSetNameOne")
                .withUpstreamTasks(upTask)
                .withDownstreamTasks(downTask)
                .build();
        DatasetLineageInfo lineageInfo = DatasetLineageInfo.newBuilder()
                .withSourceNode(nodeInfo)
                .build();
        return lineageInfo;
    }

    public static List<LineageDatasetBasic> createLineageDatasetBasicList() {
        List<LineageDatasetBasic>  lineageDatasetBasicList=new ArrayList<>();
        LineageDatasetBasic lineageDatasetBasic=new LineageDatasetBasic();
        lineageDatasetBasic.setGid(1L);
        lineageDatasetBasic.setName("testLineageDatasetBasic");
        lineageDatasetBasicList.add(lineageDatasetBasic);
        return lineageDatasetBasicList;

    }
}
