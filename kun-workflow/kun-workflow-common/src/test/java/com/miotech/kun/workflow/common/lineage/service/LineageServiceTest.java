package com.miotech.kun.workflow.common.lineage.service;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.lineage.node.DatasetNode;
import com.miotech.kun.workflow.common.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

public class LineageServiceTest extends CommonTestBase {

    private static final Logger logger = LoggerFactory.getLogger(LineageServiceTest.class);

    private static final MetadataServiceFacade mockMetadataFacade = Mockito.mock(MetadataServiceFacade.class);

    private final LineageService lineageService = new LineageService(this.neo4jSessionFactory, mockMetadataFacade);

    private <T> int countEntitiesInStorage(Class<T> nodeClass) {
        Collection<T> nodes = this.neo4jSessionFactory.openSession().loadAll(nodeClass);
        return nodes.size();
    }

    /**
     * Generate following DAG graph.
     * Note that dataset 6 & task 3 has no incoming & outgoing edge.
     *
     * +----------+
     * | Dataset1 |
     * +-----+----+
     *       |
     *       v
     * +-----+----+
     * |   Task1  |
     * +-----+----+
     *       |
     *       v
     * +-----+----+    +----------+
     * | Dataset2 |    | Dataset3 |
     * +-----+----+    +-----+----+
     *       |               |
     *       +-------+-------+
     *               v
     *         +-----+----+
     *         |   Task2  |
     *         +-----+----+
     *               |
     *      +--------+--------+
     *      v                 v
     * +----+-----+    +------+---+
     * | Dataset4 |    | Dataset5 |
     * +----------+    +----------+
     */
    private Pair<List<DatasetNode>, List<TaskNode>> prepareGraph() {
        DatasetNode datasetNode1 = MockLineageNodesFactory.getDataset();
        DatasetNode datasetNode2 = MockLineageNodesFactory.getDataset();
        DatasetNode datasetNode3 = MockLineageNodesFactory.getDataset();
        DatasetNode datasetNode4 = MockLineageNodesFactory.getDataset();
        DatasetNode datasetNode5 = MockLineageNodesFactory.getDataset();
        DatasetNode datasetNode6 = MockLineageNodesFactory.getDataset();
        List<DatasetNode> datasets = Lists.newArrayList(datasetNode1, datasetNode2, datasetNode3, datasetNode4, datasetNode5, datasetNode6);

        TaskNode task1 = MockLineageNodesFactory.getTaskNode();
        TaskNode task2 = MockLineageNodesFactory.getTaskNode();
        TaskNode task3 = MockLineageNodesFactory.getTaskNode();
        List<TaskNode> taskNodes = Lists.newArrayList(task1, task2, task3);

        // insert into graph
        datasetNode1.setAsInputOfTask(task1);
        datasetNode2.setAsOutputOfTask(task1);
        datasetNode2.setAsInputOfTask(task2);
        datasetNode3.setAsInputOfTask(task2);
        datasetNode4.setAsOutputOfTask(task2);
        datasetNode5.setAsOutputOfTask(task2);

        /**
         task1.addInlet(datasetNode1);
         task1.addOutlet(datasetNode2);
         task2.addInlet(datasetNode2);
         task2.addInlet(datasetNode3);
         task2.addOutlet(datasetNode4);
         task2.addOutlet(datasetNode5);
         */

        datasets.forEach(datasetNode -> {
            this.neo4jSessionFactory.openSession().save(datasetNode);
        });
        /**
           taskNodes.forEach(taskNode -> {
               this.neo4jSessionFactory.openSession().save(taskNode);
           });
         */

        return Pair.of(datasets, taskNodes);
    }

    @BeforeClass
    public static void init() {
        doReturn(Optional.empty()).when(mockMetadataFacade)
                .getDatasetByDatastore(Mockito.isA(DataStore.class));
    }

    @Test
    public void saveDatasetNode_whenNodeNotExists_shouldInsertDatasetNode() {
        // Prepare
        DatasetNode datasetNode = MockLineageNodesFactory.getDataset();

        // Process
        boolean isSuccess = lineageService.saveDatasetNode(datasetNode);

        // Validate
        assertTrue(isSuccess);
        Optional<DatasetNode> datasetNodeOptional = lineageService.fetchDatasetNodeById(datasetNode.getGid());
        assertTrue(datasetNodeOptional.isPresent());
        assertEquals(datasetNode, datasetNodeOptional.get());
        assertEquals(datasetNode.getGid(), datasetNodeOptional.get().getGid());
    }

    @Test
    public void saveTaskNode_whenNodeNotExists_shouldInsertTaskNode() {
        // Prepare
        TaskNode taskNode = MockLineageNodesFactory.getTaskNode();

        // Process
        boolean isSuccess = lineageService.saveTaskNode(taskNode);

        // Validate
        assertTrue(isSuccess);
        Optional<TaskNode> taskNodeOptional = lineageService.fetchTaskNodeById(taskNode.getTaskId());
        assertTrue(taskNodeOptional.isPresent());
        assertEquals(taskNode, taskNodeOptional.get());
        assertEquals(taskNode.getTaskId(), taskNodeOptional.get().getTaskId());
    }

    @Test
    public void saveDatasetNode_whenAlreadyExists_shouldUpdateDatasetNode() {
        // Prepare
        List<DatasetNode> datasetNodes = MockLineageNodesFactory.getDatasets(5);
        datasetNodes.forEach(datasetNode -> {
            assertTrue(lineageService.saveDatasetNode(datasetNode));
        });
        assertEquals(5, countEntitiesInStorage(DatasetNode.class));

        // re-insert
        datasetNodes.forEach(datasetNode -> {
            assertTrue(lineageService.saveDatasetNode(datasetNode));
        });
        // should still have the same count
        assertEquals(5, countEntitiesInStorage(DatasetNode.class));

        // new node should be inserted
        lineageService.saveDatasetNode(MockLineageNodesFactory.getDataset());
        assertEquals(6, countEntitiesInStorage(DatasetNode.class));
    }

    @Test
    public void saveTask_withNonExistNodes_shouldPersist() {
        // Prepare
        Task task = MockTaskFactory.createTask();

        // process
        lineageService.saveTask(task);

        // Validate
        assertEquals(1, countEntitiesInStorage(TaskNode.class));
        Optional<TaskNode> taskNode = lineageService.fetchTaskNodeById(task.getId());
        assertTrue(taskNode.isPresent());
        assertEquals(task.getId(), taskNode.get().getTaskId());
    }

    @Test
    public void saveDataset_withNonExistNodes_shouldPersist() {
        // Prepare
        Dataset dataset = Dataset.newBuilder()
                .withName("Example Dataset")
                .withDataStore(null)
                .withDatasourceId(1L)
                .withFields(new ArrayList<>())
                .build();
        dataset.setGid(200L);

        // process
        lineageService.saveDataset(dataset);

        // Validate
        assertEquals(1, countEntitiesInStorage(DatasetNode.class));
        Optional<DatasetNode> datasetNodeOptional = lineageService.fetchDatasetNodeById(dataset.getGid());
        assertTrue(datasetNodeOptional.isPresent());
        assertEquals(dataset.getGid(), datasetNodeOptional.get().getGid());
    }

    @Test
    public void fetchUpstreamDatasetNodes_withConstructedGraph_shouldReturnUpstreamDatasetsProperly() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();
        List<TaskNode> taskNodes = graphNodes.getRight();

        // Fetch upstream nodes of dataset 1
        Set<DatasetNode> upstreamOfDataset1 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(0).getGid());
        assertEquals(0, upstreamOfDataset1.size());

        // Fetch upstream nodes of dataset 2
        Set<DatasetNode> upstreamOfDataset2 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(1).getGid());
        assertEquals(1, upstreamOfDataset2.size());

        // task 2 should be persisted with 2 inlets & 2 outlets
        TaskNode task2 = this.neo4jSessionFactory.openSession().load(TaskNode.class, taskNodes.get(1).getTaskId());
        Set<DatasetNode> upstreamDatasetsOfTask2 = task2.getInlets();
        Set<DatasetNode> downstreamDatasetsOfTask2 = task2.getOutlets();
        assertEquals(2, upstreamDatasetsOfTask2.size());
        assertEquals(2, downstreamDatasetsOfTask2.size());

        // Fetch upstream nodes of dataset 4: dataset 2 & dataset 3
        Set<DatasetNode> upstreamOfDataset4 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(3).getGid());
        assertEquals(2, upstreamOfDataset4.size());
        Set<Long> IdsOfUpstreamOfDataset4 = upstreamDatasetsOfTask2.stream()
                .map(DatasetNode::getGid)
                .collect(Collectors.toSet());
        assertTrue(IdsOfUpstreamOfDataset4.contains(datasetNodes.get(1).getGid()));
        assertTrue(IdsOfUpstreamOfDataset4.contains(datasetNodes.get(2).getGid()));

        // Fetch 2 layers of downstream nodes of dataset 5
        Set<DatasetNode> fullUpstreamOfDataset5 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(4).getGid(), 2);
        assertEquals(3, fullUpstreamOfDataset5.size());
        Set<Long> IdsOfFullUpstreamOfDataset5 = fullUpstreamOfDataset5.stream()
                .map(DatasetNode::getGid)
                .collect(Collectors.toSet());
        assertTrue(IdsOfFullUpstreamOfDataset5.contains(datasetNodes.get(0).getGid()));
        assertTrue(IdsOfFullUpstreamOfDataset5.contains(datasetNodes.get(1).getGid()));
        assertTrue(IdsOfFullUpstreamOfDataset5.contains(datasetNodes.get(2).getGid()));
    }

    @Test
    public void fetchDownstreamDatasetNodes_withConstructedGraph_shouldReturnUpstreamDatasetsProperly() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();
        // List<TaskNode> taskNodes = graphNodes.getRight();

        // Fetch downstream nodes of dataset 1
        Set<DatasetNode> downstreamOfDataset1 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(0).getGid());
        assertEquals(1, downstreamOfDataset1.size());

        // Fetch downstream nodes of dataset 2
        Set<DatasetNode> downstreamOfDataset2 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(1).getGid());
        assertEquals(2, downstreamOfDataset2.size());

        // Fetch downstream nodes of dataset 3
        Set<DatasetNode> downstreamOfDataset3 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(2).getGid());
        assertEquals(2, downstreamOfDataset3.size());

        // Fetch downstream nodes of dataset 4
        Set<DatasetNode> downstreamOfDataset4 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(3).getGid());
        assertEquals(0, downstreamOfDataset4.size());

        // Fetch 2 layers of downstream nodes of dataset 1
        Set<DatasetNode> fullDownstreamOfDataset1 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(0).getGid(), 2);
        assertEquals(3, fullDownstreamOfDataset1.size());
    }

    @Test
    public void fetchUpstreamAndDownstreamNodes_withNotExistedId_shouldThrowException() {
        prepareGraph();
        try {
            lineageService.fetchUpstreamDatasetNodes(9999L);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }

        try {
            lineageService.fetchDownstreamDatasetNodes(9999L);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }
    }

    @Test
    public void fetchInletAndOutletNodes_withConstructedGraph_shouldReturnProperly() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        // List<DatasetNode> datasetNodes = graphNodes.getLeft();
        List<TaskNode> taskNodes = graphNodes.getRight();

        // Fetch upstream dataset nodes of task 1
        Set<DatasetNode> inputDatasetNodesOfTask1 = lineageService.fetchInletNodes(taskNodes.get(0).getTaskId());
        // Fetch downstream dataset nodes of task 1
        Set<DatasetNode> outputDatasetNodesOfTask1 = lineageService.fetchOutletNodes(taskNodes.get(0).getTaskId());
        // Fetch upstream dataset nodes of task 2
        Set<DatasetNode> inputDatasetNodesOfTask2 = lineageService.fetchInletNodes(taskNodes.get(1).getTaskId());
        // Fetch downstream dataset nodes of task 2
        Set<DatasetNode> outputDatasetNodesOfTask2 = lineageService.fetchInletNodes(taskNodes.get(1).getTaskId());
        // Fetch upstream dataset nodes of task 3
        Set<DatasetNode> inputDatasetNodesOfTask3 = lineageService.fetchInletNodes(taskNodes.get(2).getTaskId());
        // Fetch downstream dataset nodes of task 3
        Set<DatasetNode> outputDatasetNodesOfTask3 = lineageService.fetchOutletNodes(taskNodes.get(2).getTaskId());

        // Validate
        assertEquals(1, inputDatasetNodesOfTask1.size());
        assertEquals(1, outputDatasetNodesOfTask1.size());
        assertEquals(2, inputDatasetNodesOfTask2.size());
        assertEquals(2, outputDatasetNodesOfTask2.size());
        assertEquals(0, inputDatasetNodesOfTask3.size());
        assertEquals(0, outputDatasetNodesOfTask3.size());
    }

    @Test
    public void deleteDatasetNode_withExistingNodeId_shouldWorkAndReturnTrue() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();

        // Process
        boolean isSuccess = lineageService.deleteDatasetNode(datasetNodes.get(0).getGid());

        // Validate
        assertTrue(isSuccess);
        // upstream of dataset 2 should be empty
        Set<DatasetNode> upstreamOfDataset2 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(1).getGid());
        assertEquals(0, upstreamOfDataset2.size());
    }

    @Test
    public void deleteTaskNode_withExistingNodeId_shouldWorkAndReturnTrue() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();
        List<TaskNode> taskNodes = graphNodes.getRight();

        // Process
        boolean isSuccess = lineageService.deleteTaskNode(taskNodes.get(0).getTaskId());

        // Validate
        assertTrue(isSuccess);

        // upstream of dataset 2 should be empty
        Set<DatasetNode> upstreamOfDataset2 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(1).getGid());
        assertEquals(0, upstreamOfDataset2.size());
    }

    @Test
    public void deleteDatasetNode_withNonExistingNodeId_shouldReturnFalse() {
        // Prepare
        prepareGraph();

        // Process
        boolean isSuccess = lineageService.deleteDatasetNode(1234556L);

        // Validate
        assertFalse(isSuccess);
    }

    @Test
    public void deleteTaskNode_withNonExistingNodeId_shouldReturnFalse() {
        // Prepare
        prepareGraph();

        // Process
        boolean isSuccess = lineageService.deleteTaskNode(1234556L);

        // Validate
        assertFalse(isSuccess);
    }
}
