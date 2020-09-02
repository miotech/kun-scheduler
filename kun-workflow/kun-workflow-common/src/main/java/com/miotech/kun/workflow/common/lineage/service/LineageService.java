package com.miotech.kun.workflow.common.lineage.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.lineage.node.DatasetNode;
import com.miotech.kun.workflow.common.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.task.Task;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class LineageService {
    private static final Logger logger = LoggerFactory.getLogger(LineageService.class);

    private static final String ERROR_MESSAGE_NULL_ARGUMENT = "Argument cannot be null";

    private final SessionFactory sessionFactory;

    private final MetadataFacade metadataFacade;

    @Inject
    public LineageService(SessionFactory sessionFactory, MetadataFacade metadataFacade) {
        this.sessionFactory = sessionFactory;
        this.metadataFacade = metadataFacade;
    }

    // ---------------- Public methods ----------------

    public Optional<DatasetNode> fetchDatasetNodeById(Long datasetGlobalId) {
        Preconditions.checkNotNull(datasetGlobalId, ERROR_MESSAGE_NULL_ARGUMENT);
        return Optional.ofNullable(getSession().load(DatasetNode.class, datasetGlobalId));
    }

    public Optional<TaskNode> fetchTaskNodeById(Long taskId) {
        Preconditions.checkNotNull(taskId, ERROR_MESSAGE_NULL_ARGUMENT);
        return Optional.ofNullable(getSession().load(TaskNode.class, taskId));
    }

    /**
     * TODO: currently we are mocking this method.
     * @param dataStore
     * @return Dataset object
     */
    public Optional<Dataset> fetchDatasetByDatastore(DataStore dataStore) {
        return metadataFacade.fetchDatasetByDatastore(dataStore);
    }

    /**
     * Save an dataset entity to graph node and return it
     * @param dataset dataset model entity to save
     * @return saved dataset node
     */
    public DatasetNode saveDataset(Dataset dataset) {
        Optional<DatasetNode> datasetNodeOptional = fetchDatasetNodeById(dataset.getGid());
        DatasetNode datasetNode = datasetNodeOptional.orElseGet(DatasetNode::new);

        // Assign properties
        datasetNode.setGid(dataset.getGid());
        datasetNode.setDatasetName(dataset.getName());

        saveDatasetNode(datasetNode);
        return datasetNode;
    }

    /**
     * Save an task entity to graph node and return it
     * @param task task model entity to save
     * @return saved task node
     */
    public TaskNode saveTask(Task task) {
        Optional<TaskNode> taskNodeOptional = fetchTaskNodeById(task.getId());
        TaskNode taskNode = taskNodeOptional.orElseGet(() -> TaskNode.from(task));
        saveTaskNode(taskNode);
        return taskNode;
    }

    /**
     * Update or insert a dataset node into graph storage
     * @param node dataset node
     * @return <code>true</code> if operation successful
     */
    public boolean saveDatasetNode(DatasetNode node) {
        getSession().save(node);
        return true;
    }

    /**
     * Update or insert a task node into graph storage
     * @param node dataset node
     * @return <code>true</code> if operation successful
     */
    public boolean saveTaskNode(TaskNode node) {
        getSession().save(node);
        return true;
    }

    /**
     * Delete dataset node
     * @param nodeId id of dataset node
     * @return <code>true</code> if operation successful, <code>false</code> if node not found
     */
    public boolean deleteDatasetNode(Long nodeId) {
        Session sess = getSession();
        DatasetNode existingNode = sess.load(DatasetNode.class, nodeId);
        if (Objects.isNull(existingNode)) {
            return false;
        }
        // else
        sess.delete(existingNode);
        return true;
    }

    /**
     * Delete task node
     * @param nodeId id of task node
     * @return <code>true</code> if operation successful, <code>false</code> if node not found
     */
    public boolean deleteTaskNode(Long nodeId) {
        Session sess = getSession();
        TaskNode existingNode = sess.load(TaskNode.class, nodeId);
        if (Objects.isNull(existingNode)) {
            return false;
        }
        // else
        sess.delete(existingNode);
        return true;
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
     * @throws IllegalArgumentException when depth is not positive integer
     * @return set of upstream dataset nodes
     */
    public Set<DatasetNode> fetchUpstreamDatasetNodes(Long datasetGlobalId, int depth) {
        Preconditions.checkArgument(depth > 0, "Depth field should be positive but got: %s", depth);
        return searchAllRelatedDatasetNodesWithDepth(datasetGlobalId, DirectionEnum.UPSTREAM, depth);
    }

    /**
     * @param datasetGlobalId
     * @return
     */
    public Set<DatasetNode> fetchDownstreamDatasetNodes(Long datasetGlobalId) {
        return fetchDownstreamDatasetNodes(datasetGlobalId, 1);
    }

    /**
     * @param datasetGlobalId
     * @throws IllegalArgumentException when depth is not positive integer
     * @return set of downstream dataset nodes
     */
    public Set<DatasetNode> fetchDownstreamDatasetNodes(Long datasetGlobalId, int depth) {
        Preconditions.checkArgument(depth > 0, "Depth field should be positive but got: %s", depth);
        return searchAllRelatedDatasetNodesWithDepth(datasetGlobalId, DirectionEnum.DOWNSTREAM, depth);
    }

    /**
     * @param taskNodeId
     * @return
     */
    public Set<DatasetNode> fetchInletNodes(Long taskNodeId) {
        Preconditions.checkNotNull(taskNodeId, ERROR_MESSAGE_NULL_ARGUMENT);

        TaskNode taskNode = getSession().load(TaskNode.class, taskNodeId);
        if (Objects.isNull(taskNode)) {
            return new LinkedHashSet<>();
        }
        // else
        return taskNode.getInlets();
    }

    /**
     * @param taskNodeId
     * @return
     */
    public Set<DatasetNode> fetchOutletNodes(Long taskNodeId) {
        Preconditions.checkNotNull(taskNodeId, ERROR_MESSAGE_NULL_ARGUMENT);

        TaskNode taskNode = getSession().load(TaskNode.class, taskNodeId);
        if (Objects.isNull(taskNode)) {
            return new LinkedHashSet<>();
        }
        // else
        return taskNode.getOutlets();
    }

    // ---------------- Private methods ----------------

    enum DirectionEnum {
        UPSTREAM,
        DOWNSTREAM,
        BOTH,
    }

    private Session getSession() {
        return sessionFactory.openSession();
    }

    private void appendToResultSetConditionally(DatasetNode dsNode, Queue<DatasetNode> searchQueue, Set<DatasetNode> resultSet, boolean shouldAddNextDepthToQueue) {
        if (!resultSet.contains(dsNode)) {
            resultSet.add(dsNode);
            if (shouldAddNextDepthToQueue) {
                searchQueue.add(dsNode);
            }
        }
    }

    private Queue<DatasetNode> searchNeighbors(DatasetNode ds, Set<DatasetNode> resultSet, DirectionEnum direction, boolean shouldAddNextDepthToQueue) {
        Queue<DatasetNode> searchQueue = new LinkedList<>();
        if (Objects.equals(direction, DirectionEnum.DOWNSTREAM)) {
            Set<TaskNode> nextTaskNodeSet = ds.getDownstreamTasks();
            nextTaskNodeSet.forEach(taskNodePartial -> {
                Optional<TaskNode> taskNodeOptional = fetchTaskNodeById(taskNodePartial.getTaskId());
                if (!taskNodeOptional.isPresent()) {
                    throw new EntityNotFoundException(String.format("Cannot find task node with id: %s", taskNodePartial.getTaskId()));
                }
                taskNodeOptional.get()
                        .getOutlets()
                        .forEach(dsNodePartial -> {
                            DatasetNode dsNode = fetchDatasetNodeById(dsNodePartial.getGid()).orElseThrow(
                                    () -> new EntityNotFoundException(String.format("Cannot find dataset node with id: %s", dsNodePartial.getGid()))
                            );
                            appendToResultSetConditionally(dsNode, searchQueue, resultSet, shouldAddNextDepthToQueue);
                        });
            });
        }
        if (Objects.equals(direction, DirectionEnum.UPSTREAM)){
            Set<TaskNode> nextTaskNodeSet = ds.getUpstreamTasks();
            nextTaskNodeSet.forEach(taskNodePartial -> {
                Optional<TaskNode> taskNodeOptional = fetchTaskNodeById(taskNodePartial.getTaskId());
                if (!taskNodeOptional.isPresent()) {
                    throw new EntityNotFoundException(String.format("Cannot find task node with id: %s", taskNodePartial.getTaskId()));
                }
                taskNodeOptional.get()
                        .getInlets()
                        .forEach(dsNodePartial -> {
                            DatasetNode dsNode = fetchDatasetNodeById(dsNodePartial.getGid()).orElseThrow(
                                    () -> new EntityNotFoundException(String.format("Cannot find dataset node with id: %s", dsNodePartial.getGid()))
                            );
                            appendToResultSetConditionally(dsNode, searchQueue, resultSet, shouldAddNextDepthToQueue);
                        });
            });
        }
        return searchQueue;
    }

    private Set<DatasetNode> searchAllRelatedDatasetNodesWithDepth(Long datasetGlobalId, DirectionEnum direction, int depth) {
        Preconditions.checkNotNull(datasetGlobalId, "Invalid argument `datasetGlobalId`: null");
        Preconditions.checkNotNull(direction, "Invalid argument `direction`: null");
        Preconditions.checkArgument(depth >= 1, "Invalid argument `depth`: %s; should be positive integer.", depth);

        Session sess = getSession();
        DatasetNode datasetNode = sess.load(DatasetNode.class, datasetGlobalId);
        if (Objects.isNull(datasetNode)) {
            throw new EntityNotFoundException(String.format("Cannot find dataset node with id: %s", datasetGlobalId));
        }
        Set<DatasetNode> resultSet = new LinkedHashSet<>();

        Queue<DatasetNode> searchQueue = new LinkedList<>();
        searchQueue.add(datasetNode);

        for (int currentDepth = 1; currentDepth <= depth; currentDepth += 1) {
            int depthElementsSize = searchQueue.size();
            for (int i = 0; i < depthElementsSize; i += 1) {
                DatasetNode ds = searchQueue.poll();
                logger.debug(String.format("Searching dataset node with id = %s, currentDepth = %s, depth = %s", ds.getGid(), currentDepth, depth));
                if (Objects.equals(direction, DirectionEnum.DOWNSTREAM) || Objects.equals(direction, DirectionEnum.BOTH)) {
                    Queue<DatasetNode> nextSearchQueue = searchNeighbors(ds, resultSet, DirectionEnum.DOWNSTREAM, currentDepth < depth);
                    searchQueue.addAll(nextSearchQueue);
                }
                if (Objects.equals(direction, DirectionEnum.UPSTREAM) || Objects.equals(direction, DirectionEnum.BOTH)) {
                    Queue<DatasetNode> nextSearchQueue = searchNeighbors(ds, resultSet, DirectionEnum.UPSTREAM, currentDepth < depth);
                    searchQueue.addAll(nextSearchQueue);
                }
            }
        }

        return resultSet;
    }
}
