package com.miotech.kun.dataplatform.web.common.deploy.service;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.DataPlatformTestBase;
import com.miotech.kun.dataplatform.facade.model.commit.CommitType;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskRelation;
import com.miotech.kun.dataplatform.mocking.*;
import com.miotech.kun.dataplatform.web.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.web.common.deploy.dao.DeployedTaskDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskRelationDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.facade.sla.SlaFacade;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.testing.WithMockTestUser;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunDAG;
import com.miotech.kun.workflow.client.model.TaskRunDependency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@WithMockTestUser
public class DeployedTaskServiceTest extends DataPlatformTestBase {

    @SpyBean
    private DeployedTaskService deployedTaskService;

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Autowired
    private DeployedTaskDao deployedTaskDao;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private SlaFacade slaFacade;

    @Autowired
    private TaskRelationDao taskRelationDao;

    @Test
    public void test_find_failed() {
        try {
            deployedTaskService.find(1L);
        } catch (Throwable e) {
            assertThat(e.getClass(), is(IllegalArgumentException.class));
            assertThat(e.getMessage(), is("Deployed Task not found: \"1\""));
        }
    }

    @Test
    public void test_deployTask_created() {
        // mock
        Task mockTask = MockWorkflowTaskFactory.mockTask(1L);
        doReturn(mockTask).when(workflowClient).saveTask(any(), anyList());

        TaskCommit commit = MockTaskCommitFactory.createTaskCommit();
        taskCommitDao.create(commit);
        // invocation
        deployedTaskService.deployTask(commit);

        // verify
        DeployedTask deployedTask = deployedTaskService.find(commit.getDefinitionId());
        assertThat(deployedTask.getWorkflowTaskId(), is(mockTask.getId()));
        assertThat(deployedTask.getOwner(), is(commit.getSnapshot().getOwner()));
        assertThat(deployedTask.getDefinitionId(), is(commit.getDefinitionId()));
        assertThat(deployedTask.getTaskTemplateName(), is(commit.getSnapshot().getTaskTemplateName()));
    }

    @Test
    public void test_deployTask_created_node_existed() {
        // mock
        Task mockTask = MockWorkflowTaskFactory.mockTask(1L);
        doReturn(mockTask).when(workflowClient).saveTask(any(), anyList());

        Long taskDefId = IdGenerator.getInstance().nextId();
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition(taskDefId, MockSlaFactory.create());
        TaskCommit commit = MockTaskCommitFactory.createTaskCommit(taskDefinition);
        taskCommitDao.create(commit);

        // create task-definition node
        slaFacade.save(MockTaskDefinitionNodeFactory.create(commit.getDefinitionId()));
        // invocation
        deployedTaskService.deployTask(commit);

        // verify
        DeployedTask deployedTask = deployedTaskService.find(commit.getDefinitionId());
        assertThat(deployedTask.getWorkflowTaskId(), is(mockTask.getId()));
        assertThat(deployedTask.getOwner(), is(commit.getSnapshot().getOwner()));
        assertThat(deployedTask.getDefinitionId(), is(commit.getDefinitionId()));
        assertThat(deployedTask.getTaskTemplateName(), is(commit.getSnapshot().getTaskTemplateName()));
        TaskDefinitionNode node = slaFacade.findById(taskDefId);
        assertThat(node, notNullValue());
        assertThat(node.getId(), is(taskDefId));
        assertThat(node.getName(), is(taskDefinition.getName()));
        SlaConfig slaConfig = commit.getSnapshot().getTaskPayload().getScheduleConfig().getSlaConfig();
        assertThat(node.getLevel(), is(slaConfig.getLevel()));
        assertThat(node.getDeadline(), is(slaConfig.getDeadlineValue(commit.getSnapshot().getTaskPayload().getScheduleConfig().getTimeZone())));
        assertThat(node.getWorkflowTaskId(), is(deployedTask.getWorkflowTaskId()));
    }

    @Test
    public void test_deployTask_created_alreadyExist() {
        // mock
        Task mockTask = MockWorkflowTaskFactory.mockTask(1L);
        doReturn(mockTask).when(workflowClient).saveTask(any(), anyList());

        TaskCommit commit = MockTaskCommitFactory.createTaskCommit();
        taskCommitDao.create(commit);
        DeployedTask mockDeployedTask = MockDeployedTaskFactory.createDeployedTask().cloneBuilder()
                .withTaskCommit(commit)
                .withDefinitionId(commit.getDefinitionId()).build();
        deployedTaskDao.create(mockDeployedTask);

        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition().cloneBuilder()
                .withDefinitionId(commit.getDefinitionId()).build();
        taskDefinitionDao.create(taskDefinition);

        // invocation
        deployedTaskService.deployTask(commit);

        // verify
        DeployedTask deployedTask = deployedTaskService.find(commit.getDefinitionId());
        assertThat(deployedTask.getWorkflowTaskId(), is(mockTask.getId()));
        assertThat(deployedTask.getOwner(), is(commit.getSnapshot().getOwner()));
        assertThat(deployedTask.getDefinitionId(), is(commit.getDefinitionId()));
        assertThat(deployedTask.getTaskTemplateName(), is(commit.getSnapshot().getTaskTemplateName()));
    }

    @Test
    public void test_deployTask_modified() {
        // mock
        Task mockTask = MockWorkflowTaskFactory.mockTask(1L);
        doReturn(mockTask).when(workflowClient).saveTask(any(), anyList());

        TaskCommit commit = MockTaskCommitFactory.createTaskCommit()
                .cloneBuilder()
                .withCommitType(CommitType.MODIFIED)
                .build();
        taskCommitDao.create(commit);
        DeployedTask deployedTask = deployedTaskService.deployTask(commit);
        assertThat(deployedTask.getWorkflowTaskId(), is(mockTask.getId()));
        assertThat(deployedTask.getOwner(), is(commit.getSnapshot().getOwner()));
        assertThat(deployedTask.getDefinitionId(), is(commit.getDefinitionId()));
        assertThat(deployedTask.getTaskTemplateName(), is(commit.getSnapshot().getTaskTemplateName()));
    }

    @Test
    public void test_deployTask_offlined_before_created() {
        TaskCommit commit = MockTaskCommitFactory.createTaskCommit()
                .cloneBuilder()
                .withCommitType(CommitType.OFFLINE)
                .build();
        taskCommitDao.create(commit);

        try {
            deployedTaskService.deployTask(commit);
        } catch (Throwable e) {
            assertThat(e.getClass(), is(IllegalArgumentException.class));
            assertThat(e.getMessage(), is(String.format("Could'nt offline deployed task, deployed task not found: %s", commit.getDefinitionId())));
        }
    }

    @Test
    public void test_deployTask_offlined_after_created() {
        // mock
        Task mockTask = MockWorkflowTaskFactory.mockTask(1L);
        doReturn(mockTask).when(workflowClient).saveTask(any(), anyList());
        doReturn(mockTask).when(workflowClient).getTask(anyLong());

        // create and deploy
        TaskCommit commit = MockTaskCommitFactory.createTaskCommit();
        taskCommitDao.create(commit);
        deployedTaskService.deployTask(commit);

        // offline
        TaskCommit nextCommit = commit.cloneBuilder()
                .withId(DataPlatformIdGenerator.nextCommitId())
                .withCommitType(CommitType.OFFLINE)
                .build();
        taskCommitDao.create(nextCommit);
        deployedTaskService.deployTask(nextCommit);

        // verify
        DeployedTask deployedTask = deployedTaskService.find(commit.getDefinitionId());
        assertThat(deployedTask.getWorkflowTaskId(), is(mockTask.getId()));
        assertThat(deployedTask.getOwner(), is(commit.getSnapshot().getOwner()));
        assertThat(deployedTask.getDefinitionId(), is(commit.getDefinitionId()));
        assertThat(deployedTask.getTaskTemplateName(), is(commit.getSnapshot().getTaskTemplateName()));
        assertThat(deployedTask.isArchived(), is(true));
    }

    @Test
    public void test_deployTask_withDependency() {
        // mock
        doAnswer((i) -> {
            Task task = i.getArgument(0, Task.class);
            Task.Builder taskBuilder = task.cloneBuilder().withId(IdGenerator.getInstance().nextId());
            return taskBuilder.build();
        }).when(workflowClient).saveTask(any(), anyList());

        // create and deploy
        int taskSize = 4;
        List<TaskCommit> commits = MockTaskCommitFactory.createTaskCommit(taskSize);
        commits.forEach(taskCommitDao::create);

        List<DeployedTask> deployedTasks = commits.stream()
                .map(deployedTaskService::deployTask)
                .collect(Collectors.toList());

        // verify
        assertThat(deployedTasks.size(), is(taskSize));
    }

    @Test
    public void testGetUserByTaskId_exist() {
        // insert test data
        TaskCommit mockTaskCommit = MockTaskCommitFactory.createTaskCommit();
        taskCommitDao.create(mockTaskCommit);

        DeployedTask mockDeployedTask = MockDeployedTaskFactory.createDeployedTask().cloneBuilder()
                .withTaskCommit(mockTaskCommit)
                .withDefinitionId(mockTaskCommit.getDefinitionId()).build();
        deployedTaskDao.create(mockDeployedTask);

        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition().cloneBuilder()
                .withDefinitionId(mockTaskCommit.getDefinitionId()).build();
        taskDefinitionDao.create(taskDefinition);


        // mock interface
        UserInfo userInfo = new UserInfo();
        String username = "test_username";
        userInfo.setUsername(username);
        doReturn(userInfo).when(deployedTaskService).getUserById(anyLong());

        // execute
        UserInfo userInfoOfFetched = deployedTaskService.getUserByTaskId(mockDeployedTask.getWorkflowTaskId());

        // verify
        assertThat(userInfoOfFetched.getUsername(), is(userInfo.getUsername()));
    }

    @Test
    public void testFetchUnarchived() {
        List<DeployedTask> result = MockDeployedTaskFactory.createDeployedTask(2);
        result.forEach(x -> {
            taskCommitDao.create(x.getTaskCommit());
            deployedTaskDao.create(x);
        });

        List<DeployedTask> unarchived = deployedTaskService.fetchUnarchived();
        assertThat(unarchived.size(), is(2));

        // set archived=true
        DeployedTask deployedTask = result.get(0);
        DeployedTask archivedDeployedTask = deployedTask.cloneBuilder().withArchived(true).build();
        deployedTaskDao.update(archivedDeployedTask);

        unarchived = deployedTaskService.fetchUnarchived();
        assertThat(unarchived.size(), is(1));
    }

    @Test
    public void testRebuildRelationship() {
        // mock
        doAnswer((i) -> {
            Task task = i.getArgument(0, Task.class);
            Task.Builder taskBuilder = task.cloneBuilder().withId(IdGenerator.getInstance().nextId());
            return taskBuilder.build();
        }).when(workflowClient).saveTask(any(), anyList());

        // create and deploy
        int taskSize = 2;
        List<TaskCommit> commits = MockTaskCommitFactory.createTaskCommit(taskSize);
        commits.forEach(taskCommitDao::create);
        commits.forEach(tc -> taskDefinitionDao.create(MockTaskDefinitionFactory.createTaskDefinition(tc.getDefinitionId(), null)));
        commits.stream()
                .map(deployedTaskService::deployTask)
                .collect(Collectors.toList());
        taskRelationDao.create(ImmutableList.of(new TaskRelation(commits.get(0).getDefinitionId(), commits.get(1).getDefinitionId(), OffsetDateTime.now(), OffsetDateTime.now())));

        // rebuild relationship
        deployedTaskService.rebuildRelationship();

        // validate
        TaskCommit taskCommit = commits.get(0);
        TaskDefinitionNode node = slaFacade.findById(taskCommit.getDefinitionId());
        assertThat(node, notNullValue());
        assertThat(node.getId(), is(taskCommit.getDefinitionId()));
    }

    @Test
    public void getUpstreamTaskRuns_shouldOk() {
        //prepare
        Task task1 = MockWorkflowTaskFactory.mockTask(1L);
        Task task2 = MockWorkflowTaskFactory.mockTask(1L);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskRunDAG taskRunDAG = new TaskRunDAG();
        taskRunDAG.setNodes(Arrays.asList(taskRun1, taskRun2));
        taskRunDAG.setEdges(Collections.singletonList(new TaskRunDependency(taskRun2.getId(), taskRun1.getId())));

        //mock
        doReturn(taskRunDAG).when(deployedTaskService).getWorkFlowTaskRunDag(anyLong(),anyInt(),anyInt());

        //execute
        List<TaskRun> upstreamTaskRun = deployedTaskService.getUpstreamTaskRuns(taskRun2.getId());

        //verify
        assertThat(upstreamTaskRun.size(), is(1));
        assertThat(upstreamTaskRun.get(0).getId(), is(taskRun1.getId()));
    }
}