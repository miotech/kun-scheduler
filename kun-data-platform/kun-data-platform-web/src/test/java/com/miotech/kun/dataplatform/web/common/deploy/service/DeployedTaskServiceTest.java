package com.miotech.kun.dataplatform.web.common.deploy.service;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.facade.model.commit.CommitType;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.mocking.MockDeployedTaskFactory;
import com.miotech.kun.dataplatform.mocking.MockTaskCommitFactory;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.mocking.MockWorkflowTaskFactory;
import com.miotech.kun.dataplatform.web.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.web.common.deploy.dao.DeployedTaskDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.testing.WithMockTestUser;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@WithMockTestUser
public class DeployedTaskServiceTest extends AppTestBase {

    @SpyBean
    private DeployedTaskService deployedTaskService;

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Autowired
    private DeployedTaskDao deployedTaskDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private WorkflowClient workflowClient;

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
        List<String> usernames = deployedTaskService.getUserByTaskId(mockDeployedTask.getWorkflowTaskId());

        // verify
        assertThat(usernames.size(), is(1));
        assertThat(usernames.get(0), is(username));
    }

}