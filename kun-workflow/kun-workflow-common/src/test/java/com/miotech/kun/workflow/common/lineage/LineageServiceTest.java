package com.miotech.kun.workflow.common.lineage;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.core.model.lineage.DatasetNodeInfo;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskBasicInformation;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskInformation;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetNode;
import com.miotech.kun.workflow.core.model.lineage.node.TaskNode;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.*;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

public class LineageServiceTest extends CommonTestBase {

    @Inject
    private LineageService lineageService;

    @Inject
    private LineageServiceFacade lineageServiceFacade;

    @Inject
    private TaskService taskService;

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    DatabaseOperator dbOperator;

    @Override
    protected void configuration() {
        super.configuration();
        bind(LineageServiceFacade.class, Mockito.mock(LineageServiceFacade.class));
        bind(MetadataServiceFacade.class, Mockito.mock(MetadataServiceFacade.class));
        bind(Scheduler.class, Mockito.mock(Scheduler.class));
    }

    @Test
    public void testFetchDirectUpstreamTask_emptyArgs() {
        // mock
        doReturn(Lists.newArrayList()).when(lineageServiceFacade).fetchDirectUpstreamTask(ImmutableList.of());

        // execute
        List<UpstreamTaskInformation> upstreamTaskInformationList =
                lineageService.fetchDirectUpstreamTask(ImmutableList.of());

        // verify
        assertThat(upstreamTaskInformationList, empty());
    }

    @Test
    public void testFetchDirectUpstreamTask() {
        // mock
        Operator mockOperator = MockOperatorFactory.createOperator();
        operatorDao.create(mockOperator);

        TaskPropsVO taskPropsVO1 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask1 = taskService.createTask(taskPropsVO1);
        TaskPropsVO taskPropsVO2 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask2 = taskService.createTask(taskPropsVO2);

        UpstreamTaskBasicInformation mockUpstreamTaskInformation = new UpstreamTaskBasicInformation(IdGenerator.getInstance().nextId(),
                ImmutableList.of(mockTask1.getId(), mockTask2.getId()));
        List<UpstreamTaskBasicInformation> mockResult = ImmutableList.of(mockUpstreamTaskInformation);
        doReturn(mockResult).when(lineageServiceFacade).fetchDirectUpstreamTask(anyList());

        // execute
        List<UpstreamTaskInformation> upstreamTaskInformationList = lineageService.fetchDirectUpstreamTask(ImmutableList.of());

        // verify
        assertThat(upstreamTaskInformationList.size(), is(1));
        UpstreamTaskInformation upstreamTaskInformation = upstreamTaskInformationList.get(0);
        assertThat(upstreamTaskInformation.getDatasetGid(), is(mockUpstreamTaskInformation.getDatasetGid()));
        assertThat(upstreamTaskInformation.getTasks().size(), is(2));
        Task taskInformation1 = upstreamTaskInformation.getTasks().get(0);
        Task taskInformation2 = upstreamTaskInformation.getTasks().get(1);
        assertThat(taskInformation1, sameBeanAs(mockTask1));
        assertThat(taskInformation2, sameBeanAs(mockTask2));
    }

    /**
     *
     * dataset0----
     *            ｜ task0
     *            ｜--------->dataset2
     *            ｜
     * dataset1---
     *
     */
    @Test
    public void test_searchLineageInfo_DOWN_1() {
        // mock
        Operator mockOperator = MockOperatorFactory.createOperator();
        operatorDao.create(mockOperator);

        TaskPropsVO taskPropsVO1 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask0 = taskService.createTask(taskPropsVO1);

        Set<DatasetNode> downDatasetNodeSet = new LinkedHashSet<>();
        DatasetNode   datasetNode0=new DatasetNode(0L,"dataset0");
        DatasetNode   datasetNode1=new DatasetNode(1L,"dataset1");
        DatasetNode   datasetNode2=new DatasetNode(2L,"dataset2");
        TaskNode taskNode0=new TaskNode(mockTask0.getId(),mockTask0.getName());
        taskNode0.addInlet(datasetNode0);
        taskNode0.addInlet(datasetNode1);
        taskNode0.addOutlet(datasetNode2);
        datasetNode0.setAsInputOfTask(taskNode0);
        datasetNode1.setAsInputOfTask(taskNode0);
        datasetNode2.setAsOutputOfTask(taskNode0);
        downDatasetNodeSet.add(datasetNode2);

        doReturn(Optional.of(datasetNode0)).when(lineageServiceFacade).fetchDatasetNodeById(datasetNode0.getGid());
        doReturn(downDatasetNodeSet).when(lineageServiceFacade).fetchDownstreamDatasetNodes(anyLong(),anyInt());
        DatasetLineageInfo datasetLineageInfoDown1 = lineageService.searchLineageInfo(datasetNode0.getGid(), 1, "DOWNSTREAM");
//       null or empty testing
        DatasetNodeInfo sourceNode = datasetLineageInfoDown1.getSourceNode();
        List<DatasetNodeInfo> downstreamNodes = datasetLineageInfoDown1.getDownstreamNodes();
        List<DatasetNodeInfo> upstreamNodes = datasetLineageInfoDown1.getUpstreamNodes();
        assertThat(sourceNode,is(notNullValue()));
        assertThat(upstreamNodes,is(empty()));
        assertThat(downstreamNodes,is(not(empty())));
//        node size testing
        assertThat(downstreamNodes.size(),is(1));
        assertThat(sourceNode.getGid(),is(datasetNode0.getGid()));
        assertThat(sourceNode.getDownstreamTasks().size(),is(datasetNode0.getDownstreamTasks().size()));
        assertThat(sourceNode.getUpstreamTasks().size(),is(datasetNode0.getUpstreamTasks().size()));
        assertThat(sourceNode.getDownstreamDatasetCount(),is(downDatasetNodeSet.size()));
        assertThat(sourceNode.getUpstreamDatasetCount(),is(0));
    }

    /**
     *
     * dataset0----
     *            ｜ task0              task1
     *            ｜--------->dataset2-------------->dataset3
     *            ｜                           ｜
     * dataset1---                            ｜---->dataset4
     *
     */
    @Test
    public void test_searchLineageInfo_depth() {
        // mock
        Operator mockOperator = MockOperatorFactory.createOperator();
        operatorDao.create(mockOperator);

        TaskPropsVO taskPropsVO1 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask0 = taskService.createTask(taskPropsVO1);

        TaskPropsVO taskPropsVO2 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask1 = taskService.createTask(taskPropsVO2);


        DatasetNode   datasetNode0=new DatasetNode(0L,"dataset0");
        DatasetNode   datasetNode1=new DatasetNode(1L,"dataset1");
        DatasetNode   datasetNode2=new DatasetNode(2L,"dataset2");
        DatasetNode   datasetNode3=new DatasetNode(3L,"dataset3");
        DatasetNode   datasetNode4=new DatasetNode(4L,"dataset4");
        TaskNode taskNode0=new TaskNode(mockTask0.getId(),mockTask0.getName());
        taskNode0.addInlet(datasetNode0);
        taskNode0.addInlet(datasetNode1);
        taskNode0.addOutlet(datasetNode2);

        TaskNode taskNode1=new TaskNode(mockTask1.getId(),mockTask1.getName());
        taskNode1.addInlet(datasetNode2);
        taskNode1.addOutlet(datasetNode3);
        taskNode1.addOutlet(datasetNode4);

        datasetNode0.setAsInputOfTask(taskNode0);
        datasetNode1.setAsInputOfTask(taskNode0);
        datasetNode2.setAsOutputOfTask(taskNode0);
        datasetNode2.setAsInputOfTask(taskNode1);
        datasetNode3.setAsOutputOfTask(taskNode1);
        datasetNode4.setAsOutputOfTask(taskNode1);

        Set<DatasetNode> downDatasetNodeSet = new LinkedHashSet<>();
        downDatasetNodeSet.add(datasetNode2);

        doReturn(Optional.of(datasetNode0)).when(lineageServiceFacade).fetchDatasetNodeById(datasetNode0.getGid());
        doReturn(downDatasetNodeSet).when(lineageServiceFacade).fetchDownstreamDatasetNodes(anyLong(),anyInt());
        DatasetLineageInfo datasetLineageInfoDown2 = lineageService.searchLineageInfo(datasetNode0.getGid(), 2, "DOWNSTREAM");
//       null or empty testing
        DatasetNodeInfo sourceNode = datasetLineageInfoDown2.getSourceNode();
        List<DatasetNodeInfo> downstreamNodes = datasetLineageInfoDown2.getDownstreamNodes();
        List<DatasetNodeInfo> upstreamNodes = datasetLineageInfoDown2.getUpstreamNodes();
        assertThat(sourceNode,is(notNullValue()));
        assertThat(upstreamNodes,is(empty()));
        assertThat(downstreamNodes,is(not(empty())));
//        node size testing
        assertThat(downstreamNodes.size(),is(3));


        Set<DatasetNode> upDatasetNodeSet = new LinkedHashSet<>();
        upDatasetNodeSet.add(datasetNode2);
        doReturn(Optional.of(datasetNode4)).when(lineageServiceFacade).fetchDatasetNodeById(datasetNode4.getGid());
        doReturn(upDatasetNodeSet).when(lineageServiceFacade).fetchUpstreamDatasetNodes(anyLong(),anyInt());
        DatasetLineageInfo datasetLineageInfoUP2 = lineageService.searchLineageInfo(datasetNode4.getGid(), 2, "UPSTREAM");
        assertThat(datasetLineageInfoUP2.getUpstreamNodes().size(),is(3));

        Set<DatasetNode> bothDownDatasetNodeSet = new LinkedHashSet<>();
        bothDownDatasetNodeSet.add(datasetNode3);
        bothDownDatasetNodeSet.add(datasetNode4);
        Set<DatasetNode> bothUpDatasetNodeSet = new LinkedHashSet<>();
        bothUpDatasetNodeSet.add(datasetNode0);
        bothUpDatasetNodeSet.add(datasetNode1);
        doReturn(Optional.of(datasetNode2)).when(lineageServiceFacade).fetchDatasetNodeById(datasetNode2.getGid());
        doReturn(bothUpDatasetNodeSet).when(lineageServiceFacade).fetchUpstreamDatasetNodes(anyLong(),anyInt());
        doReturn(bothDownDatasetNodeSet).when(lineageServiceFacade).fetchDownstreamDatasetNodes(anyLong(),anyInt());
        DatasetLineageInfo datasetLineageInfoBoth = lineageService.searchLineageInfo(datasetNode2.getGid(), 1, "BOTH");
        assertThat(datasetLineageInfoBoth.getUpstreamNodes().size(),is(2));
        assertThat(datasetLineageInfoBoth.getDownstreamNodes().size(),is(2));
    }
    /**
     *
     * dataset0----
     *            ｜ task0
     *            ｜--------->dataset2
     *            ｜
     * dataset1---
     *
     */
    @Test
    public void test_searchLineageInfo_UP_1() {
        // mock
        Operator mockOperator = MockOperatorFactory.createOperator();
        operatorDao.create(mockOperator);

        TaskPropsVO taskPropsVO1 = MockTaskFactory.createTaskPropsVOWithOperator(mockOperator.getId());
        Task mockTask0 = taskService.createTask(taskPropsVO1);

        DatasetNode   datasetNode0=new DatasetNode(0L,"dataset0");
        DatasetNode   datasetNode1=new DatasetNode(1L,"dataset1");
        DatasetNode   datasetNode2=new DatasetNode(2L,"dataset2");
        TaskNode taskNode0=new TaskNode(mockTask0.getId(),mockTask0.getName());
        taskNode0.addInlet(datasetNode0);
        taskNode0.addInlet(datasetNode1);
        taskNode0.addOutlet(datasetNode2);
        datasetNode0.setAsInputOfTask(taskNode0);
        datasetNode1.setAsInputOfTask(taskNode0);
        datasetNode2.setAsOutputOfTask(taskNode0);
        Set<DatasetNode> upDatasetNodeSet = new LinkedHashSet<>();
        doReturn(Optional.of(datasetNode0)).when(lineageServiceFacade).fetchDatasetNodeById(datasetNode0.getGid());
        doReturn(upDatasetNodeSet).when(lineageServiceFacade).fetchUpstreamDatasetNodes(anyLong(),anyInt());
        DatasetLineageInfo datasetLineageInfoUP1 = lineageService.searchLineageInfo(datasetNode0.getGid(), 1, "UPSTREAM");
//       null or empty testing
        DatasetNodeInfo sourceNode = datasetLineageInfoUP1.getSourceNode();
        List<DatasetNodeInfo> downstreamNodes = datasetLineageInfoUP1.getDownstreamNodes();
        List<DatasetNodeInfo> upstreamNodes = datasetLineageInfoUP1.getUpstreamNodes();
        assertThat(sourceNode,is(notNullValue()));
        assertThat(upstreamNodes,is(empty()));
        assertThat(downstreamNodes,is(empty()));
//        node size testing
        assertThat(sourceNode.getGid(),is(datasetNode0.getGid()));
        assertThat(sourceNode.getDownstreamTasks().size(),is(datasetNode0.getDownstreamTasks().size()));
        assertThat(sourceNode.getUpstreamTasks().size(),is(datasetNode0.getUpstreamTasks().size()));
        assertThat(sourceNode.getDownstreamDatasetCount(),is(1));
        assertThat(sourceNode.getUpstreamDatasetCount(),is(0));
    }


}
