package com.miotech.kun.metadata.common.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.exception.EntityNotFoundException;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.factory.MockLineageNodesFactory;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskBasicInformation;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetNode;
import com.miotech.kun.workflow.core.model.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@Testcontainers
public class LineageServiceTest extends DatabaseTestBase {

    private static final Logger logger = LoggerFactory.getLogger(LineageServiceTest.class);

    private MetadataDatasetService metadataDatasetService;

    @Container
    public static Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
            .withAdminPassword("Mi0tech2020");

    protected SessionFactory neo4jSessionFactory = initNeo4jSessionFactory();

    @Inject
    private LineageService lineageService;

    @Override
    protected void configuration() {
        bind(SessionFactory.class, neo4jSessionFactory);
        metadataDatasetService = mock(MetadataDatasetService.class);
        bind(MetadataDatasetService.class,metadataDatasetService);
        super.configuration();
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
        clearGraph();
    }

    private void clearGraph() {
        Session session = neo4jSessionFactory.openSession();
        // delete all nodes with relationships
        session.query("match (n) -[r] -> () delete n, r;", new HashMap<>());
        // delete nodes that have no relationships
        session.query("match (n) delete n;", new HashMap<>());
    }

    private <T> int countEntitiesInStorage(Class<T> nodeClass) {
        Collection<T> nodes = this.neo4jSessionFactory.openSession().loadAll(nodeClass);
        return nodes.size();
    }

    public SessionFactory initNeo4jSessionFactory() {
        Configuration config = new Configuration.Builder()
                .uri(neo4jContainer.getBoltUrl())
                .connectionPoolSize(50)
                .credentials("neo4j", "Mi0tech2020")
                .build();
        return new SessionFactory(config, GraphDatabaseModule.DEFAULT_NEO4J_DOMAIN_CLASSES);
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
        Long taskId = WorkflowIdGenerator.nextTaskId();
        Task task = Task.newBuilder()
                .withId(taskId)
                .withName("task_" + taskId)
                .withDescription("task_description_" + taskId)
                .withConfig(Config.EMPTY)
                .withOperatorId(WorkflowIdGenerator.nextOperatorId())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withQueueName("default")
                .withPriority(16)
                .withRetries(0)
                .withRetryDelay(0)
                .build();

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
                .withGid(200L)
                .withName("Example Dataset")
                .withDataStore(null)
                .withDatasourceId(1L)
                .withFields(new ArrayList<>())
                .build();

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
        DatasetNode datasetNode1 = datasetNodes.get(0);
        Set<DatasetNode> upstreamOfDataset1 = lineageService.fetchUpstreamDatasetNodes(datasetNode1.getGid(),1);
        assertEquals(0, upstreamOfDataset1.size());

        // Fetch upstream nodes of dataset 2
        DatasetNode datasetNode2 = datasetNodes.get(1);
        Set<DatasetNode> upstreamOfDataset2 = lineageService.fetchUpstreamDatasetNodes(datasetNode2.getGid(),1);
        assertEquals(1, upstreamOfDataset2.size());
        DatasetNode dataNode1 = upstreamOfDataset2.iterator().next();
        assertThat(dataNode1.getGid(),is(datasetNode1.getGid()));
        assertThat(dataNode1.getDatasetName(),is(datasetNode1.getDatasetName()));


        Set<TaskNode> downstreamTasks1 = dataNode1.getDownstreamTasks();
        TaskNode taskNode1 = downstreamTasks1.iterator().next();
        TaskNode beforeTaskNode = datasetNode1.getDownstreamTasks().iterator().next();
        assertThat(taskNode1.getTaskId(),is(beforeTaskNode.getTaskId()));
        assertThat(taskNode1.getTaskName(),is(beforeTaskNode.getTaskName()));

        Set<DatasetNode> taskNode1Inlets = taskNode1.getInlets();
        assertThat(taskNode1Inlets.size(),is(1));
        DatasetNode  taskNode1INlet = taskNode1Inlets.iterator().next();
        assertThat(taskNode1INlet.getGid(),is(dataNode1.getGid()));

        Set<DatasetNode> taskNode1Outlets = taskNode1.getOutlets();
        assertThat(taskNode1Outlets.size(),is(0));


        Set<TaskNode> upstreamTasks1 = dataNode1.getUpstreamTasks();
        assertThat(upstreamTasks1.size(),is(0));




        // task 2 should be persisted with 2 inlets & 2 outlets
        TaskNode task2 = this.neo4jSessionFactory.openSession().load(TaskNode.class, taskNodes.get(1).getTaskId(),1);
        Set<DatasetNode> upstreamDatasetsOfTask2 = task2.getInlets();
        Set<DatasetNode> downstreamDatasetsOfTask2 = task2.getOutlets();
        assertEquals(2, upstreamDatasetsOfTask2.size());
        assertEquals(2, downstreamDatasetsOfTask2.size());

        // Fetch upstream nodes of dataset 4: dataset 2 & dataset 3
        Set<DatasetNode> upstreamOfDataset4 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(3).getGid(),1);
        assertEquals(2, upstreamOfDataset4.size());
        Set<Long> IdsOfUpstreamOfDataset4 = upstreamDatasetsOfTask2.stream()
                .map(DatasetNode::getGid)
                .collect(Collectors.toSet());
        assertTrue(IdsOfUpstreamOfDataset4.contains(datasetNode2.getGid()));
        assertTrue(IdsOfUpstreamOfDataset4.contains(datasetNodes.get(2).getGid()));

        // Fetch 2 layers of downstream nodes of dataset 5
        Set<DatasetNode> fullUpstreamOfDataset5 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(4).getGid(), 2);
        assertEquals(3, fullUpstreamOfDataset5.size());
        Set<Long> IdsOfFullUpstreamOfDataset5 = fullUpstreamOfDataset5.stream()
                .map(DatasetNode::getGid)
                .collect(Collectors.toSet());
        assertTrue(IdsOfFullUpstreamOfDataset5.contains(datasetNode1.getGid()));
        assertTrue(IdsOfFullUpstreamOfDataset5.contains(datasetNode2.getGid()));
        assertTrue(IdsOfFullUpstreamOfDataset5.contains(datasetNodes.get(2).getGid()));
    }
    @Test
    public void fetchUpstreamDatasetNodes_checkUpStreamRelationship() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();
        List<TaskNode> taskNodes = graphNodes.getRight();

        // Fetch upstream nodes of dataset 1
        DatasetNode datasetNode1 = datasetNodes.get(0);
        // Fetch upstream nodes of dataset 2
        DatasetNode datasetNode2 = datasetNodes.get(1);
        Set<DatasetNode> upstream1 = lineageService.fetchUpstreamDatasetNodes(datasetNode2.getGid(),1);
        assertEquals(1, upstream1.size());
        DatasetNode dataNode1 = upstream1.iterator().next();
        assertThat(dataNode1.getGid(),is(datasetNode1.getGid()));
        assertThat(dataNode1.getDatasetName(),is(datasetNode1.getDatasetName()));

        Set<TaskNode> upstreamTasks1 = dataNode1.getUpstreamTasks();
        assertThat(upstreamTasks1.size(),is(0));

        Set<TaskNode> downstreamTasks1 = dataNode1.getDownstreamTasks();
        TaskNode taskNode1 = downstreamTasks1.iterator().next();
        TaskNode beforeTaskNode = datasetNode1.getDownstreamTasks().iterator().next();
        assertThat(taskNode1.getTaskId(),is(beforeTaskNode.getTaskId()));
        assertThat(taskNode1.getTaskName(),is(beforeTaskNode.getTaskName()));

        Set<DatasetNode> taskNode1Inlets = taskNode1.getInlets();
        assertThat(taskNode1Inlets.size(),is(1));
        DatasetNode  taskNode1INlet = taskNode1Inlets.iterator().next();
        assertThat(taskNode1INlet.getGid(),is(dataNode1.getGid()));

        Set<DatasetNode> taskNode1Outlets = taskNode1.getOutlets();
        assertThat(taskNode1Outlets.size(),is(0));

    }
    @Test
    public void fetchDownStreamDatasetNodes_checkDepth() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();
        List<TaskNode> taskNodes = graphNodes.getRight();

        // Fetch upstream nodes of dataset 1
        DatasetNode datasetNode1 = datasetNodes.get(0);
        Set<DatasetNode> downstreamDepth1 = lineageService.fetchDownstreamDatasetNodes(datasetNode1.getGid(),1);
        assertThat(downstreamDepth1.size(),is(1));
        Set<DatasetNode> downstreamDepth3 = lineageService.fetchDownstreamDatasetNodes(datasetNode1.getGid(),2);
        assertThat(downstreamDepth3.size(),is(3));

        Set<DatasetNode> downstreamDepth5 = lineageService.fetchDownstreamDatasetNodes(datasetNode1.getGid(),5);
        assertThat(downstreamDepth5.size(),is(3));
        // Fetch upstream nodes of dataset 3
        DatasetNode datasetNode3 = datasetNodes.get(2);
        Set<DatasetNode> downstreamDepthDataNode3 = lineageService.fetchDownstreamDatasetNodes(datasetNode3.getGid(),5);
        assertThat(downstreamDepthDataNode3.size(),is(2));

    }

    @Test
    public void fetchUpStreamDatasetNodes_checkDepth() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();
        List<TaskNode> taskNodes = graphNodes.getRight();

        // Fetch upstream nodes of dataset 1
        DatasetNode datasetNode1 = datasetNodes.get(0);
        Set<DatasetNode> up1Depth1 = lineageService.fetchUpstreamDatasetNodes(datasetNode1.getGid(), 1);
        assertThat(up1Depth1.size(),is(0));
        Set<DatasetNode> up1Depth2 = lineageService.fetchUpstreamDatasetNodes(datasetNode1.getGid(),2);
        assertThat(up1Depth2.size(),is(0));
        // Fetch upstream nodes of dataset 2
        DatasetNode datasetNode2 = datasetNodes.get(1);
        Set<DatasetNode> up2Depth1 = lineageService.fetchUpstreamDatasetNodes(datasetNode2.getGid(),1);
        assertThat(up2Depth1.size(),is(1));
        Set<DatasetNode> up2Depth3 = lineageService.fetchUpstreamDatasetNodes(datasetNode2.getGid(),3);
        assertThat(up2Depth3.size(),is(1));
        // Fetch upstream nodes of dataset 4
        DatasetNode datasetNode4 = datasetNodes.get(3);
        Set<DatasetNode> up4Depth1 = lineageService.fetchUpstreamDatasetNodes(datasetNode4.getGid(),1);
        assertThat(up4Depth1.size(),is(2));
        Set<DatasetNode> up4Depth2 = lineageService.fetchUpstreamDatasetNodes(datasetNode4.getGid(),2);
        assertThat(up4Depth2.size(),is(3));
        Set<DatasetNode> up4Depth5 = lineageService.fetchUpstreamDatasetNodes(datasetNode4.getGid(),5);
        assertThat(up4Depth5.size(),is(3));


    }

    @Test
    public void fetchDownstreamDatasetNodes_withConstructedGraph_shouldReturnUpstreamDatasetsProperly() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<DatasetNode> datasetNodes = graphNodes.getLeft();
        // List<TaskNode> taskNodes = graphNodes.getRight();

        // Fetch downstream nodes of dataset 1
        Set<DatasetNode> downstreamOfDataset1 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(0).getGid(),1);
        assertEquals(1, downstreamOfDataset1.size());

        // Fetch downstream nodes of dataset 2
        Set<DatasetNode> downstreamOfDataset2 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(1).getGid(),1);
        assertEquals(2, downstreamOfDataset2.size());

        // Fetch downstream nodes of dataset 3
        Set<DatasetNode> downstreamOfDataset3 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(2).getGid(),1);
        assertEquals(2, downstreamOfDataset3.size());

        // Fetch downstream nodes of dataset 4
        Set<DatasetNode> downstreamOfDataset4 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(3).getGid(),1);
        assertEquals(0, downstreamOfDataset4.size());

        // Fetch 2 layers of downstream nodes of dataset 1
        Set<DatasetNode> fullDownstreamOfDataset1 = lineageService.fetchDownstreamDatasetNodes(datasetNodes.get(0).getGid(), 2);
        assertEquals(3, fullDownstreamOfDataset1.size());
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
        Set<DatasetNode> upstreamOfDataset2 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(1).getGid(),1);
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
        Set<DatasetNode> upstreamOfDataset2 = lineageService.fetchUpstreamDatasetNodes(datasetNodes.get(1).getGid(),1);
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

    @Test
    public void fetchEdgeInfo_withExistingLineage_shouldReturnCorrectEdgeInfo() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> preparedGraph = prepareGraph();
        List<DatasetNode> datasetNodes = preparedGraph.getLeft();
        List<TaskNode> taskNodes = preparedGraph.getRight();

        Long dataset1Gid = datasetNodes.get(0).getGid();
        Long dataset2Gid = datasetNodes.get(1).getGid();
        Long dataset3Gid = datasetNodes.get(2).getGid();
        Long task1Id = taskNodes.get(0).getTaskId();

        // Process
        EdgeInfo edgeInfoFromDataset1ToDataset2 = lineageService.fetchEdgeInfo(dataset1Gid, dataset2Gid);
        EdgeInfo edgeInfoFromDataset2ToDataset3 = lineageService.fetchEdgeInfo(dataset2Gid, dataset3Gid);

        // Validate
        assertThat(edgeInfoFromDataset1ToDataset2.getTaskInfos().size(), is(1));
        assertThat(edgeInfoFromDataset2ToDataset3.getTaskInfos().size(), is(0));
        assertThat(edgeInfoFromDataset1ToDataset2.getTaskInfos().get(0).getId(), is(task1Id));
    }

    @Test
    public void testUpdateLineage(){
        // Prepare
        Long upstreamDataSetId = IdGenerator.getInstance().nextId();
        DataStore upstreamStore = MockDatasetFactory.createDataStore("Hive","upstreamStore","table");
        Dataset upstreamSet = MockDatasetFactory.createDatasetWithDataStore(upstreamDataSetId,"upstreamSet",1l,null,upstreamStore);
        Long downstreamDataSetId = IdGenerator.getInstance().nextId();
        DataStore downstreamStore = MockDatasetFactory.createDataStore("Hive","downstreamStore","table");
        Dataset downstreamSet = MockDatasetFactory.createDatasetWithDataStore(downstreamDataSetId,"downstreamSet",1l,null,downstreamStore);
        //mock fetch dataset by datastore
        doAnswer(invocation -> {
            DataStore dataStore = invocation.getArgument(0,DataStore.class);
            if(dataStore.getDatabaseName().equals(upstreamStore.getDatabaseName())){
                return upstreamSet;
            }
            if(dataStore.getDatabaseName().equals(downstreamStore.getDatabaseName())){
                return downstreamSet;
            }
            return null;
        }).when(metadataDatasetService).createDataSetIfNotExist(any(DataStore.class));

        Long taskId = WorkflowIdGenerator.nextTaskId();
        Task task = Task.newBuilder()
                .withId(taskId)
                .withName("task_" + taskId)
                .withDescription("task_description_" + taskId)
                .withConfig(Config.EMPTY)
                .withOperatorId(WorkflowIdGenerator.nextOperatorId())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withQueueName("default")
                .withPriority(16)
                .withRetries(0)
                .withRetryDelay(0)
                .build();;

        // Process
        lineageService.updateTaskLineage(task, Lists.newArrayList(upstreamStore),Lists.newArrayList(downstreamStore));

        //verify
        TaskNode taskNode = lineageService.fetchTaskNodeById(taskId).get();
        assertThat(taskNode.getTaskId(), Is.is(taskId));
        Set<DatasetNode> upstreamDataSets = lineageService.fetchInletNodes(taskNode.getTaskId());
        Set<DatasetNode> downstreamDataSets = lineageService.fetchOutletNodes(taskNode.getTaskId());
        assertThat(upstreamDataSets,hasSize(1));
        DatasetNode upstreamDataSet = upstreamDataSets.iterator().next();
        assertThat(upstreamDataSet.getGid(),is(upstreamDataSetId));
        assertThat(upstreamDataSet.getDatasetName(),is(upstreamSet.getName()));

        assertThat(downstreamDataSets,hasSize(1));
        DatasetNode downstreamDataSet = downstreamDataSets.iterator().next();
        assertThat(downstreamDataSet.getGid(),is(downstreamDataSetId));
        assertThat(downstreamDataSet.getDatasetName(),is(downstreamSet.getName()));
    }

    @Test
    public void testFetchDirectTask_empty() {
        List<UpstreamTaskBasicInformation> upstreamTaskInformationList = lineageService.fetchDirectUpstreamTask(ImmutableList.of());
        assertThat(upstreamTaskInformationList, empty());
    }

    @Test
    public void testFetchDirectTask() {
        // Prepare
        Pair<List<DatasetNode>, List<TaskNode>> graphNodes = prepareGraph();
        List<Long> datasetGids = graphNodes.getLeft().stream().map(datasetNode -> datasetNode.getGid()).collect(Collectors.toList());

        // Execute
        List<UpstreamTaskBasicInformation> upstreamTaskInformationList = lineageService.fetchDirectUpstreamTask(datasetGids);

        // Verify
        assertThat(upstreamTaskInformationList.size(), is(3));
    }

}
