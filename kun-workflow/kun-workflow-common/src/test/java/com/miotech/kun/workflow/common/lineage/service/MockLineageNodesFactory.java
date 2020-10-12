package com.miotech.kun.workflow.common.lineage.service;

import com.miotech.kun.workflow.common.lineage.node.DatasetNode;
import com.miotech.kun.workflow.common.lineage.node.TaskNode;

import java.util.ArrayList;
import java.util.List;

public class MockLineageNodesFactory {
    private static long idCounter = 0;

    public static List<DatasetNode> getDatasets(int count) {
        List<DatasetNode> collection = new ArrayList<>();
        for (int i = 0; i < count; i += 1) {
            idCounter += 1;
            DatasetNode datasetNode = new DatasetNode();
            datasetNode.setGid(idCounter + 100L);
            datasetNode.setDatasetName("dataset-" + idCounter);
            collection.add(datasetNode);
        }
        return collection;
    }

    public static List<TaskNode> getTaskNodes(int count) {
        List<TaskNode> collection = new ArrayList<>();
        for (int i = 0; i < count; i += 1) {
            idCounter += 1;
            TaskNode taskNode = new TaskNode(idCounter, "task-" + idCounter, "task-description-" + idCounter);
            collection.add(taskNode);
        }
        return collection;
    }

    public static DatasetNode getDataset() {
        return getDatasets(1).get(0);
    }

    public static TaskNode getTaskNode() {
        return getTaskNodes(1).get(0);
    }
}
