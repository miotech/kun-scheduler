package com.miotech.kun.workflow.common.lineage.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.core.model.lineage.*;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetNode;
import com.miotech.kun.workflow.core.model.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.task.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class LineageService {

    private final MetadataServiceFacade metadataFacade;

    private final LineageServiceFacade lineageFacade;


    private final TaskDao taskDao;

    @Inject
    public LineageService(MetadataServiceFacade metadataFacade, LineageServiceFacade lineageFacade, TaskDao taskDao) {
        this.metadataFacade = metadataFacade;
        this.lineageFacade = lineageFacade;
        this.taskDao = taskDao;
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
        return Optional.ofNullable(metadataFacade.createDataSetIfNotExist(dataStore));
    }


    /**
     * Obtain edge info by upstream and downstream dataset id
     *
     * @param upstreamDatasetGid   global id of upstream dataset
     * @param downstreamDatasetGid global id of downstream dataset
     */
    public EdgeInfo fetchEdgeInfo(Long upstreamDatasetGid, Long downstreamDatasetGid) {
        return lineageFacade.fetchEdgeInfo(upstreamDatasetGid, downstreamDatasetGid);
    }

    /**
     * Delete task node
     *
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


    /**
     * Batch query the upstream tasks corresponding to the dataset
     *
     * @param datasetGids
     * @return
     */
    public List<UpstreamTaskInformation> fetchDirectUpstreamTask(List<Long> datasetGids) {
        List<UpstreamTaskBasicInformation> upstreamTaskBasicInformationList = lineageFacade.fetchDirectUpstreamTask(datasetGids);
        Set<Long> taskIds = upstreamTaskBasicInformationList.stream()
                .flatMap(basicInfo -> basicInfo.getTaskIds().stream())
                .collect(Collectors.toSet());
        Map<Long, Optional<Task>> taskMap = taskDao.fetchByIds(taskIds);

        return upstreamTaskBasicInformationList.stream().map(upstreamTaskInformation -> {
            List<Task> taskList = upstreamTaskInformation.getTaskIds().stream()
                    .filter(taskId -> taskMap.get(taskId).isPresent())
                    .map(taskId -> {
                        Optional<Task> taskOpt = taskMap.get(taskId);
                        return taskOpt.get();
                    }).collect(Collectors.toList());
            return new UpstreamTaskInformation(upstreamTaskInformation.getDatasetGid(), taskList);
        }).collect(Collectors.toList());
    }

    public DatasetLineageInfo searchLineageInfo(Long datasetGid, int depth, String direction) {
        Set<DatasetNode> upstreamNodes = new LinkedHashSet<>();
        Set<DatasetNode> downstreamNodes = new LinkedHashSet<>();
        Optional<DatasetNode> sourceNode = fetchDatasetNodeById(datasetGid);
        DatasetNode datasetNode = sourceNode.orElseThrow(() -> new EntityNotFoundException("dataset does not exists, id:" + datasetGid));

        if (StringUtils.containsAny(direction, "UPSTREAM", "BOTH")) {
            Set<DatasetNode> upstreamDatasetNodes = fetchUpstreamDatasetNodes(datasetGid, depth + 1);
            tileDataSetNodeSet(upstreamDatasetNodes, upstreamNodes, 1, depth, true);
        }
        if (StringUtils.containsAny(direction, "DOWNSTREAM", "BOTH")) {
            Set<DatasetNode> downstreamDatasetNodes = fetchDownstreamDatasetNodes(datasetGid, depth + 1);
            tileDataSetNodeSet(downstreamDatasetNodes, downstreamNodes, 1, depth, false);
        }
        Map<Long, Optional<Task>> idToTaskMap = getIdToTaskMap(upstreamNodes, downstreamNodes, datasetNode);
        DatasetNodeInfo sourceNodeInfo = datasetNodeToInfo(datasetNode, idToTaskMap).cloneBuilder()
                .withUpstreamDatasetCount(upstreamNodes.size()).withDownstreamDatasetCount(downstreamNodes.size()).build();
        return DatasetLineageInfo.newBuilder()
                .withSourceNode(sourceNodeInfo)
                .withUpstreamNodes(datasetNodesToInfoList(upstreamNodes, idToTaskMap))
                .withDownstreamNodes(datasetNodesToInfoList(downstreamNodes, idToTaskMap))
                .withQueryDepth(depth)
                .build();
    }

    private Map<Long, Optional<Task>> getIdToTaskMap(Set<DatasetNode> upstreamNodes,
                                                     Set<DatasetNode> downstreamNodes,
                                                     DatasetNode datasetNode) {
        Set<Long> relatedTaskIds = new HashSet<>();
        getRelatedTaskId(relatedTaskIds, datasetNode);
        upstreamNodes.forEach(node -> getRelatedTaskId(relatedTaskIds, node));
        downstreamNodes.forEach(node -> getRelatedTaskId(relatedTaskIds, node));
        return taskDao.fetchByIds(relatedTaskIds);
    }

    private void getRelatedTaskId(Set<Long> relatedTaskIds, DatasetNode sourceNode) {
        relatedTaskIds.addAll(sourceNode.getUpstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toSet()));
        relatedTaskIds.addAll(sourceNode.getDownstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toSet()));
    }

    private List<DatasetNodeInfo> datasetNodesToInfoList(Set<DatasetNode> datasetNodes, Map<Long, Optional<Task>> idToTaskMap) {

        return datasetNodes.stream()
                .map(node -> this.datasetNodeToInfo(node, idToTaskMap))
                .collect(Collectors.toList());
    }


    private static void tileDataSetNodeSet(Set<DatasetNode> rootDateSetNodeSet, Set<DatasetNode> datasetNodes, int currentDepth, int depth, Boolean isUp) {
        if (currentDepth > depth) {
            return;
        }
        if (CollectionUtils.isEmpty(rootDateSetNodeSet)) {
            return;
        }
        currentDepth++;
        for (DatasetNode datasetNode : rootDateSetNodeSet) {
            if (datasetNodes.contains(datasetNode)) {
                break;
            }
            datasetNodes.add(datasetNode);
            for (TaskNode taskNode : isUp ? datasetNode.getUpstreamTasks() : datasetNode.getDownstreamTasks()) {
                Set<DatasetNode> nodeSet = isUp ? taskNode.getInlets() : taskNode.getOutlets();
                if (CollectionUtils.isEmpty(nodeSet)) {
                    break;
                }
                tileDataSetNodeSet(nodeSet, datasetNodes, currentDepth, depth, isUp);
            }
        }
    }

    private DatasetNodeInfo datasetNodeToInfo(DatasetNode datasetNode, Map<Long, Optional<Task>> idToTaskMap) {
        // Set upstream/downstream tasks to empty if direction is not matching
        return DatasetNodeInfo.newBuilder()
                .withGid(datasetNode.getGid())
                .withDatasetName(datasetNode.getDatasetName())
                .withUpstreamTasks(datasetNode.getUpstreamTasks().stream()
                        .map(taskNode -> idToTaskMap.getOrDefault(taskNode.getTaskId(), Optional.empty()).orElse(null))
                        .collect(Collectors.toList()))
                .withDownstreamTasks(datasetNode.getDownstreamTasks().stream()
                        .map(taskNode -> idToTaskMap.getOrDefault(taskNode.getTaskId(), Optional.empty()).orElse(null))
                        .collect(Collectors.toList()))
                .withUpstreamDatasetCount(getCount(datasetNode.getUpstreamTasks(), TaskNode::getInlets))
                .withDownstreamDatasetCount(getCount(datasetNode.getDownstreamTasks(), TaskNode::getOutlets))
                .build();
    }

    private int getCount(Set<TaskNode> upstreamTaskNodes, Function<TaskNode, Set<DatasetNode>> linkDatasetNode) {
        return Math.toIntExact(upstreamTaskNodes.stream()
                .map(linkDatasetNode)
                .filter(CollectionUtils::isNotEmpty)
                .map(Set::size).mapToInt(Integer::new).sum());
    }
}
