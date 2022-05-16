package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeTaskInfo;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: return  workflowclient mock data
 * @author: zemin  huang
 * @create: 2022-01-26 17:12
 **/
public class MockWorkFlowClientDataFactory {

    public static Task getTask(long id, String testDownTask) {
        return Task.newBuilder().withId(id).withName(testDownTask)
                .withDependencies(new ArrayList<>()).build();

    }

    public static EdgeInfo createEdgeInfo(ArrayList<EdgeTaskInfo> taskInfos) {
        EdgeInfo edgeInfo = EdgeInfo.newBuilder()
                .withTaskInfos(taskInfos)
                .withUpstreamDatasetGid(1L)
                .withDownstreamDatasetGid(1L)
                .build();
        return edgeInfo;
    }


    public static EdgeTaskInfo getEdgeTaskInfo(Long taskId, String taskName) {
        EdgeTaskInfo taskInfo_1 = EdgeTaskInfo.newBuilder()
                .withId(taskId)
                .withName(taskName)
                .build();
        return taskInfo_1;
    }

    public static TaskRun getTaskRun(long id, String dateTimeString, TaskRunStatus success) {
        TaskRun taskRun = TaskRun.newBuilder().withId(id).withStartAt(DateTimeUtils.freezeAt(dateTimeString))
                .withStatus(success).build();
        return taskRun;
    }

    public static Map<Long, List<TaskRun>> createLatestTaskRuns(Long taskId, List<TaskRun> taskRunList) {
        Map<Long, List<TaskRun>> map = new HashMap<>();
        List<TaskRun> taskRuns = taskRunList.stream().sorted((r1, r2) -> r2.getId().compareTo(r1.getId())).collect(Collectors.toList());
        map.put(taskId, taskRuns);
        return map;
    }

    public static DatasetLineageInfo createLineageNeighborsGraph(DatasetNodeInfo nodeInfo, List<DatasetNodeInfo> up, List<DatasetNodeInfo> down) {

        DatasetLineageInfo lineageInfo = DatasetLineageInfo.newBuilder()
                .withSourceNode(nodeInfo)
                .withUpstreamNodes(up)
                .withDownstreamNodes(down)
                .build();
        return lineageInfo;
    }


    public static DatasetNodeInfo getDatasetNodeInfo(Long gid, String dataSetName) {
        DatasetNodeInfo upNodeInfo = DatasetNodeInfo
                .newBuilder()
                .withGid(gid)
                .withDatasetName(dataSetName)
                .withDownstreamDatasetCount(1)
                .withUpstreamDatasetCount(1)
                .build();
        return upNodeInfo;
    }

    public static DatasetLineageInfo createLineageNeighbors(ArrayList<Task> upTask, ArrayList<Task> downTask, Long dataSetId) {
        DatasetNodeInfo nodeInfo = DatasetNodeInfo
                .newBuilder()
                .withGid(dataSetId)
                .withDatasetName("taskInfoOne")
                .withUpstreamTasks(upTask)
                .withDownstreamTasks(downTask)
                .build();
        DatasetLineageInfo lineageInfo = DatasetLineageInfo.newBuilder()
                .withSourceNode(nodeInfo)
                .build();
        return lineageInfo;
    }

    public static List<DatasetBasicInfo> createLineageDatasetBasicList(List<DatasetNodeInfo> datasetNodeInfoList) {
        List<DatasetBasicInfo> datasetBasicInfoList = datasetNodeInfoList.stream().map(datasetNodeInfo -> {
            DatasetBasicInfo lineageDatasetBasic = new DatasetBasicInfo();
            lineageDatasetBasic.setGid(datasetNodeInfo.getGid());
            lineageDatasetBasic.setName(datasetNodeInfo.getDatasetName());
            return lineageDatasetBasic;
        }).collect(Collectors.toList());
        return datasetBasicInfoList;

    }


}
