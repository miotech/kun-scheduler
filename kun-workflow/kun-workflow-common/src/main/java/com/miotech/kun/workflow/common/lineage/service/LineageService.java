package com.miotech.kun.workflow.common.lineage.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetNode;
import com.miotech.kun.workflow.core.model.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Singleton
public class LineageService {
    private static final Logger logger = LoggerFactory.getLogger(LineageService.class);

    private final MetadataServiceFacade metadataFacade;

    private final LineageServiceFacade lineageFacade;

    @Inject
    public LineageService(MetadataServiceFacade metadataFacade,LineageServiceFacade lineageFacade) {
        this.metadataFacade = metadataFacade;
        this.lineageFacade = lineageFacade;
    }

    // ---------------- Public methods ----------------

    public Optional<DatasetNode> fetchDatasetNodeById(Long datasetGlobalId) {
        return lineageFacade.fetchDatasetNodeById(datasetGlobalId);
    }

    /**
     * Fetch count of upstream dataset nodes that have direct lineage relation to specific dataset
     *
     * @param datasetGlobalId source dataset node
     * @return count of upstream dataset nodes that have direct lineage relation to the source dataset node
     */
    public Integer fetchDatasetDirectUpstreamCount(Long datasetGlobalId) {
        return lineageFacade.fetchDatasetDirectUpstreamCount(datasetGlobalId);
    }

    /**
     * Fetch count of upstream dataset nodes that have direct lineage relation to specific dataset
     *
     * @param datasetGlobalId source dataset node
     * @return count of upstream dataset nodes that have direct lineage relation to the source dataset node
     */
    public Integer fetchDatasetDirectDownstreamCount(Long datasetGlobalId) {
        return lineageFacade.fetchDatasetDirectDownstreamCount(datasetGlobalId);
    }

    public Optional<TaskNode> fetchTaskNodeById(Long taskId) {
        return lineageFacade.fetchTaskNodeById(taskId);
    }

    /**
     * Obtain dataset by datastore object as key
     *
     * @param dataStore datastore object which represents dataset
     * @return Optional dataset object
     */
    public Optional<Dataset> fetchDatasetByDatastore(DataStore dataStore) {
        return Optional.ofNullable(metadataFacade.getDatasetByDatastore(dataStore));
    }


    /**
     * Obtain edge info by upstream and downstream dataset id
     *
     * @param upstreamDatasetGid   global id of upstream dataset
     * @param downstreamDatasetGid global id of downstream dataset
     */
    public EdgeInfo fetchEdgeInfo(Long upstreamDatasetGid, Long downstreamDatasetGid) {
        return lineageFacade.fetchEdgeInfo(upstreamDatasetGid,downstreamDatasetGid);
    }

    /**
     * Delete task node
     * @param nodeId id of task node
     * @return <code>true</code> if operation successful, <code>false</code> if node not found
     */
    public boolean deleteTaskNode(Long nodeId) {
        return lineageFacade.deleteTaskNode(nodeId);
    }

    /**
     * @param datasetGlobalId
     * @return
     */
    public Set<DatasetNode> fetchUpstreamDatasetNodes(Long datasetGlobalId) {
        return fetchUpstreamDatasetNodes(datasetGlobalId, 1);
    }

    /**
     * @param datasetGlobalId
     * @return set of upstream dataset nodes
     * @throws IllegalArgumentException when depth is not positive integer
     */
    public Set<DatasetNode> fetchUpstreamDatasetNodes(Long datasetGlobalId, int depth) {
        return lineageFacade.fetchUpstreamDatasetNodes(datasetGlobalId, depth);
    }


    /**
     * @param datasetGlobalId
     * @return set of downstream dataset nodes
     * @throws IllegalArgumentException when depth is not positive integer
     */
    public Set<DatasetNode> fetchDownstreamDatasetNodes(Long datasetGlobalId, int depth) {
        return lineageFacade.fetchDownstreamDatasetNodes(datasetGlobalId, depth);
    }

    /**
     * @param taskNodeId
     * @return
     */
    public Set<DatasetNode> fetchInletNodes(Long taskNodeId) {
        return lineageFacade.fetchInletNodes(taskNodeId);
    }

    /**
     * @param taskNodeId
     * @return
     */
    public Set<DatasetNode> fetchOutletNodes(Long taskNodeId) {
        return lineageFacade.fetchOutletNodes(taskNodeId);
    }

    /**
     * @param task
     * @param upstreamDatastore
     * @param downstreamDataStore
     * @return
     */
    public void updateTaskLineage(Task task, List<DataStore> upstreamDatastore, List<DataStore> downstreamDataStore) {
        lineageFacade.updateTaskLineage(task, upstreamDatastore, downstreamDataStore);
    }

}
