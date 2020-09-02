package com.miotech.kun.dataplatform.common.deploy.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.common.commit.service.TaskCommitService;
import com.miotech.kun.dataplatform.common.commit.vo.CommitRequest;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployRequest;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.mocking.MockTaskCommitFactory;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.deploy.Deploy;
import com.miotech.kun.dataplatform.model.deploy.DeployCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployStatus;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.security.testing.WithMockTestUser;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

// TODO: figure out a solution to bootstrap Workflow facade related tests
@Ignore
@WithMockTestUser
public class DeployServiceTest extends AppTestBase {

    @Autowired
    private DeployService deployService;

    @Autowired
    private TaskCommitService taskCommitService;

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private DeployedTaskService deployedTaskService;

    @Test
    public void test_find_failed() {
        try {
            deployService.find(1L);
        } catch (Throwable e) {
            assertThat(e.getClass(), is(IllegalArgumentException.class));
            assertThat(e.getMessage(), is("Deploy not found: \"1\""));
        }
    }

    @Test
    public void test_deployfast_ok() {
        // prepare
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);

        String msg = "Create test message";
        CommitRequest request = new CommitRequest(taskDefinition.getDefinitionId(), msg);

        // invocation
        Deploy deploy = deployService.deployFast(taskDefinition.getDefinitionId(), request);

        assertTrue(deploy.getId() > 0);
        assertThat(deploy.getName(), is(taskDefinition.getName()));
        assertThat(deploy.getStatus(), is(DeployStatus.SUCCESS));
        assertTrue(!deploy.getCommits().isEmpty());

        DeployCommit deployCommit = deploy.getCommits().get(0);
        assertThat(deployCommit.getDeployId(), is(deploy.getId()));
        assertThat(deployCommit.getDeployStatus(), is(DeployStatus.SUCCESS));
    }

    @Test
    public void find() {
    }

    @Test
    public void test_create_ok() {
        // prepare
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        String msg = "Create test message";
        TaskCommit commit = taskCommitService.commit(taskDefinition.getDefinitionId(), msg);
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setCommitIds(Collections.singletonList(commit.getId()));
        // deploy
        Deploy deploy = deployService.create(deployRequest);

        // verify
        assertTrue(deploy.getId() > 0);
        assertThat(deploy.getName(), is(taskDefinition.getName()));
        assertThat(deploy.getStatus(), is(DeployStatus.CREATED));
        assertFalse(deploy.getCommits().isEmpty());

        DeployCommit deployCommit = deploy.getCommits().get(0);
        assertThat(deployCommit.getDeployId(), is(deploy.getId()));
        assertThat(deployCommit.getCommit(), is(commit.getId()));
        assertThat(deployCommit.getDeployStatus(), is(DeployStatus.CREATED));
    }

    @Test
    public void test_publish_ok() {
        // prepare
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        String msg = "Create test message";
        TaskCommit commit = taskCommitService.commit(taskDefinition.getDefinitionId(), msg);
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setCommitIds(Collections.singletonList(commit.getId()));

        // invocation
        Deploy deploy = deployService.create(deployRequest);
        Deploy result = deployService.publish(deploy.getId());

        // verify
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), is(taskDefinition.getName()));
        assertThat(result.getStatus(), is(DeployStatus.SUCCESS));
        assertFalse(result.getCommits().isEmpty());

        DeployCommit deployCommit = result.getCommits().get(0);
        assertThat(deployCommit.getDeployId(), is(result.getId()));
        assertThat(deployCommit.getCommit(), is(commit.getId()));
        assertThat(deployCommit.getDeployStatus(), is(DeployStatus.SUCCESS));

        DeployedTask deployedTask = deployedTaskService.find(commit.getDefinitionId());
        assertNotNull(deployedTask.getWorkflowTaskId());
    }

    @Test
    public void test_publish_withDependency_ok() {
        // prepare
        List<TaskCommit> commits = MockTaskCommitFactory.createTaskCommit(4);
        commits.forEach(taskCommitDao::create);

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setCommitIds(commits.stream().map(TaskCommit::getId).collect(Collectors.toList()));

        // invocation
        Deploy deploy = deployService.create(deployRequest);
        Deploy result = deployService.publish(deploy.getId());

        // verify
        assertTrue(result.getId() > 0);
        assertThat(result.getStatus(), is(DeployStatus.SUCCESS));
        assertThat(result.getCommits().size(), is(4));

        TaskCommit commit = commits.get(0);
        DeployCommit deployCommit = result.getCommits().get(0);
        assertThat(deployCommit.getDeployId(), is(result.getId()));
        assertThat(deployCommit.getCommit(), is(commit.getId()));
        assertThat(deployCommit.getDeployStatus(), is(DeployStatus.SUCCESS));

        DeployedTask deployedTask = deployedTaskService.find(commit.getDefinitionId());
        assertNotNull(deployedTask.getWorkflowTaskId());
    }
}