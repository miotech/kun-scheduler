package com.miotech.kun.metadata.common.rpc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.service.LineageService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
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
public class MetadataServiceFacadeImpl implements MetadataServiceFacade {
    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceFacadeImpl.class);

    @Inject
    MetadataDatasetService metadataDatasetService;

    @Inject
    LineageService lineageService;


    @Override
    public Dataset getDatasetByDatastore(DataStore datastore) {
        return metadataDatasetService.getDatasetByDatastore(datastore);
    }

    @Override
    public Optional<DatasetNode> fetchDatasetNodeById(Long datasetGlobalId) {
        return Optional.empty();
    }

    @Override
    public Integer fetchDatasetDirectUpstreamCount(Long datasetGlobalId) {
        return lineageService.fetchDatasetDirectUpstreamCount(datasetGlobalId);
    }

    @Override
    public Integer fetchDatasetDirectDownstreamCount(Long datasetGlobalId) {
        return lineageService.fetchDatasetDirectDownstreamCount(datasetGlobalId);
    }

    @Override
    public Optional<TaskNode> fetchTaskNodeById(Long taskId) {
        return lineageService.fetchTaskNodeById(taskId);
    }

    @Override
    public EdgeInfo fetchEdgeInfo(Long upstreamDatasetGid, Long downstreamDatasetGid) {
        return lineageService.fetchEdgeInfo(upstreamDatasetGid, downstreamDatasetGid);
    }

    @Override
    public DatasetNode saveDataset(Dataset dataset) {
        return lineageService.saveDataset(dataset);
    }

    @Override
    public boolean deleteTaskNode(Long nodeId) {
        return lineageService.deleteTaskNode(nodeId);
    }

    @Override
    public Set<DatasetNode> fetchUpstreamDatasetNodes(Long datasetGlobalId, int depth) {
        return lineageService.fetchUpstreamDatasetNodes(datasetGlobalId, depth);
    }

    @Override
    public Set<DatasetNode> fetchDownstreamDatasetNodes(Long datasetGlobalId, int depth) {
        return lineageService.fetchDownstreamDatasetNodes(datasetGlobalId, depth);
    }

    @Override
    public Set<DatasetNode> fetchInletNodes(Long taskNodeId) {
        return lineageService.fetchInletNodes(taskNodeId);
    }

    @Override
    public Set<DatasetNode> fetchOutletNodes(Long taskNodeId) {
        return lineageService.fetchOutletNodes(taskNodeId);
    }

    @Override
    public void updateTaskLineage(Task task, List<DataStore> upstreamDatastore, List<DataStore> downstreamDataStore) {
        lineageService.updateTaskLineage(task, upstreamDatastore, downstreamDataStore);
    }

}
