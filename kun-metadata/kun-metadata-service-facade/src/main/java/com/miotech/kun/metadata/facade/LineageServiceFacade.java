package com.miotech.kun.metadata.facade;

import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskBasicInformation;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetNode;
import com.miotech.kun.workflow.core.model.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.task.Task;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LineageServiceFacade {


    Optional<DatasetNode> fetchDatasetNodeById(Long datasetGlobalId);

    /**
     * Fetch count of upstream dataset nodes that have direct lineage relation to specific dataset
     *
     * @param datasetGlobalId source dataset node
     * @return count of upstream dataset nodes that have direct lineage relation to the source dataset node
     */
    Integer fetchDatasetDirectUpstreamCount(Long datasetGlobalId);

    /**
     * Fetch count of upstream dataset nodes that have direct lineage relation to specific dataset
     *
     * @param datasetGlobalId source dataset node
     * @return count of upstream dataset nodes that have direct lineage relation to the source dataset node
     */
    Integer fetchDatasetDirectDownstreamCount(Long datasetGlobalId);

    Optional<TaskNode> fetchTaskNodeById(Long taskId);


    /**
     * Obtain edge info by upstream and downstream dataset id
     *
     * @param upstreamDatasetGid   global id of upstream dataset
     * @param downstreamDatasetGid global id of downstream dataset
     */
    EdgeInfo fetchEdgeInfo(Long upstreamDatasetGid, Long downstreamDatasetGid);


    /**
     * Save an dataset entity to graph node and return it
     *
     * @param dataset dataset model entity to save
     * @return saved dataset node
     */
    DatasetNode saveDataset(Dataset dataset);

    /**
     * Delete task node
     * @param nodeId id of task node
     * @return <code>true</code> if operation successful, <code>false</code> if node not found
     */
    boolean deleteTaskNode(Long nodeId);

    /**
     * @param datasetGlobalId
     * @return set of upstream dataset nodes
     * @throws IllegalArgumentException when depth is not positive integer
     */
    Set<DatasetNode> fetchUpstreamDatasetNodes(Long datasetGlobalId, int depth);

    /**
     * @param datasetGlobalId
     * @return set of downstream dataset nodes
     * @throws IllegalArgumentException when depth is not positive integer
     */
    Set<DatasetNode> fetchDownstreamDatasetNodes(Long datasetGlobalId, int depth);

    /**
     * @param taskNodeId
     * @return
     */
    Set<DatasetNode> fetchInletNodes(Long taskNodeId);

    /**
     * @param taskNodeId
     * @return
     */
    Set<DatasetNode> fetchOutletNodes(Long taskNodeId);

    /**
     * update task's lineage by it's input dataStore and output dataStore
     * @param task
     * @param upstreamDatastore
     * @param downstreamDataStore
     * @return
     */
    void updateTaskLineage(Task task, List<DataStore> upstreamDatastore, List<DataStore> downstreamDataStore);


    /**
     * Batch query the upstream tasks corresponding to the dataset
     * @param datasetGids
     * @return
     */
    List<UpstreamTaskBasicInformation> fetchDirectUpstreamTask(List<Long> datasetGids);
}
