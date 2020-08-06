package com.miotech.kun.dataplatform.common.deploy.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployedTaskDAG;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployedTaskDependencyVO;
import com.miotech.kun.dataplatform.common.deploy.vo.ScheduledTaskRunSearchRequest;
import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.common.utils.TagUtils;
import com.miotech.kun.dataplatform.mocking.MockTaskCommitFactory;
import com.miotech.kun.dataplatform.model.commit.CommitType;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;
import com.miotech.kun.security.testing.WithMockTestUser;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@WithMockTestUser
public class DeployedTaskServiceTest extends AppTestBase {

    @Autowired
    private DeployedTaskService deployedTaskService;

    @Autowired
    private TaskCommitDao taskCommitDao;

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
        TaskCommit commit = MockTaskCommitFactory.createTaskCommit();
        taskCommitDao.create(commit);
        // invocation
        deployedTaskService.deployTask(commit);

        // verify
        DeployedTask deployedTask = deployedTaskService.find(commit.getDefinitionId());
        assertTrue(deployedTask.getId() > 0);
        assertTrue(deployedTask.getWorkflowTaskId() > 0);

        Task task = deployedTaskService.getWorkFlowTask(deployedTask.getDefinitionId());

        // verify
        assertTrue(task.getId() > 0);
        assertThat(task.getName(), is(deployedTask.getName()));
        assertThat(task.getDescription(), is("Deployed Data Platform Task : " + deployedTask.getDefinitionId()));
        assertThat(task.getScheduleConf(), sameBeanAs(new ScheduleConf(ScheduleType.SCHEDULED, "0 0 10 * * ?")));
        assertThat(task.getDependencies().size(), is(0));
        assertThat(task.getConfig().getString("sparkSQL"), is("SELECT 1 AS T"));

        Comparator<Tag> tagComparator = Comparator.comparing(Tag::getKey);
        List<Tag> prodTag = TagUtils.buildScheduleRunTags(
                deployedTask.getDefinitionId(),
                deployedTask.getTaskCommit().getId(),
                deployedTask.getTaskTemplateName(),
                deployedTask.getOwner(),
                false);
        task.getTags().sort(tagComparator);
        prodTag.sort(tagComparator);
        assertThat(task.getTags(), is(prodTag));
    }

    @Test
    public void test_deployTask_modified() {
        TaskCommit commit = MockTaskCommitFactory.createTaskCommit()
                .cloneBuilder()
                .withCommitType(CommitType.MODIFIED)
                .build();
        taskCommitDao.create(commit);
        DeployedTask deployedTask = deployedTaskService.deployTask(commit);
        assertTrue(deployedTask.getId() > 0);
        assertTrue(deployedTask.getWorkflowTaskId() > 0);

        Task task = deployedTaskService.getWorkFlowTask(deployedTask.getDefinitionId());
        assertTrue(task.getId() > 0);
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

        assertTrue(deployedTask.getId() > 0);
        assertTrue(deployedTask.getWorkflowTaskId() > 0);
        assertTrue(deployedTask.isArchived());

        Task task = deployedTaskService.getWorkFlowTask(deployedTask.getDefinitionId());
        ScheduleConf invalidSchedule = new ScheduleConf(ScheduleType.NONE, "");
        assertThat(task.getScheduleConf(), sameBeanAs(invalidSchedule));
        assertThat(task.getDependencies().size(), is(0));
    }

    @Test
    public void test_deployTask_withDependency() {
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
    public void test_getDeployedTaskDag() {
        // prepare
        int taskSize = 3;
        List<TaskCommit> commits = MockTaskCommitFactory.createTaskCommit(taskSize);
        commits.forEach(taskCommitDao::create);

        List<DeployedTask> deployedTasks = commits.stream()
                .map(deployedTaskService::deployTask)
                .collect(Collectors.toList());

        DeployedTaskDAG dagTasks;

        // start from the first task, downstream 2
        dagTasks = deployedTaskService.getDeployedTaskDag(deployedTasks.get(0).getDefinitionId(), 0, 2);
        assertThat(dagTasks.getNodes().size(), is(taskSize));
        List<DeployedTaskDependencyVO> edges = dagTasks.getEdges();
        assertThat(edges.size(), is(2));
        assertThat(edges.get(0).getUpstreamTaskId(), is(deployedTasks.get(0).getDefinitionId()));
        assertThat(edges.get(0).getDownStreamTaskId(), is(deployedTasks.get(1).getDefinitionId()));
        assertThat(edges.get(1).getUpstreamTaskId(), is(deployedTasks.get(1).getDefinitionId()));
        assertThat(edges.get(1).getDownStreamTaskId(), is(deployedTasks.get(2).getDefinitionId()));

        // start from the first task, downstream 1
        dagTasks = deployedTaskService.getDeployedTaskDag(deployedTasks.get(0).getDefinitionId(), 0, 1);
        assertThat(dagTasks.getNodes().size(), is(2));

        // start from the second task, upstream and downstream 1
        dagTasks = deployedTaskService.getDeployedTaskDag(deployedTasks.get(1).getDefinitionId(), 1, 1);
        assertThat(dagTasks.getNodes().size(), is(taskSize));

        // start from the third task, upstream 2
        dagTasks = deployedTaskService.getDeployedTaskDag(deployedTasks.get(2).getDefinitionId(), 2, 0);
        assertThat(dagTasks.getNodes().size(), is(taskSize));

        // start from the third task, upstream 1
        dagTasks = deployedTaskService.getDeployedTaskDag(deployedTasks.get(2).getDefinitionId(), 1, 0);
        assertThat(dagTasks.getNodes().size(), is(2));
    }

    @Test
    public void test_searchTaskRuns_ok() {
        ScheduledTaskRunSearchRequest searchRequest = new ScheduledTaskRunSearchRequest(
                10, 1,
                Optional.empty(),
                Collections.emptyList(),
                null,
                null,
                TaskRunStatus.CREATED,
                null,
                null
        );

        PaginationResult<TaskRun> taskruns = deployedTaskService.searchTaskRun(searchRequest);
        assertThat(taskruns.getTotalCount(), is(0L));
    }
}