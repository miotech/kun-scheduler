package com.miotech.kun.monitor.sla.timeline.service;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.commit.TaskSnapshot;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.web.common.deploy.dao.DeployedTaskDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.monitor.sla.AppTestBase;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.common.service.TaskTimelineService;
import com.miotech.kun.monitor.sla.mocking.*;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

public class TaskTimelineServiceTest extends AppTestBase {

    @Autowired
    private TaskTimelineService taskTimelineService;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskTimelineDao taskTimelineDao;

    @Autowired
    private DeployedTaskDao deployedTaskDao;

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Test
    public void testFetchByDeadline_empty() {
        SlaConfig slaConfig = MockSlaFactory.create();
        List<TaskTimeline> taskTimelines = taskTimelineService.fetchByDeadline(slaConfig.getDeadline(ZoneId.systemDefault().getId()));
        assertThat(taskTimelines.size(), is(0));
    }

    @Test
    public void testFetchByDeadline_createThenFetch() {
        TaskTimeline taskTimeline = MockTaskTimelineFactory.createTaskTimeline(1L);
        taskTimelineService.create(taskTimeline);

        List<TaskTimeline> taskTimelines = taskTimelineService.fetchByDeadline(taskTimeline.getDeadline());
        assertThat(taskTimelines.size(), is(1));
        TaskTimeline taskTimelineOfFetch = taskTimelines.get(0);
        assertThat(taskTimelineOfFetch, sameBeanAs(taskTimeline).ignoring("id").ignoring("rootDefinitionId"));
    }

    @Test
    public void testHandleTaskRunCreatedEvent_notDeployed() {
        // Build DeployedTask, But do not write to the database
        DeployedTask deployedTask = MockDeployedTaskFactory.createDeployedTask();

        TaskRunCreatedEvent event = new TaskRunCreatedEvent(IdGenerator.getInstance().nextId(), IdGenerator.getInstance().nextId());
        taskTimelineService.handleTaskRunCreatedEvent(event);

        List<TaskTimeline> taskTimelines = taskTimelineDao.fetchByDefinitionId(deployedTask.getDefinitionId());
        assertTrue(taskTimelines.isEmpty());
    }

    @Test
    public void testHandleTaskRunCreatedEvent_deployedThenFetchedWithoutBaseline() {
        // Build DeployedTask, then write to the database
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition(IdGenerator.getInstance().nextId(), null);
        TaskCommit taskCommit = MockTaskCommitFactory.createTaskCommit(taskDefinition);
        DeployedTask deployedTask = MockDeployedTaskFactory.createDeployedTask(taskCommit);
        prepareDeployedTask(deployedTask);

        // Prepare
        prepareTaskDefinitionWithOutBaseline(deployedTask.getDefinitionId());

        TaskRunCreatedEvent event = new TaskRunCreatedEvent(deployedTask.getWorkflowTaskId(), IdGenerator.getInstance().nextId());
        taskTimelineService.handleTaskRunCreatedEvent(event);

        List<TaskTimeline> taskTimelines = taskTimelineDao.fetchByDefinitionId(deployedTask.getDefinitionId());
        assertTrue(taskTimelines.isEmpty());
    }

    @Test
    public void testHandleTaskRunCreatedEvent_deployedThenFetchedWithBaseline() {
        // Build DeployedTask, then write to the database
        DeployedTask deployedTask = MockDeployedTaskFactory.createDeployedTask();
        prepareDeployedTask(deployedTask);

        SlaConfig slaConfig = deployedTask.getTaskCommit().getSnapshot().getTaskPayload().getScheduleConfig().getSlaConfig();
        TaskRunCreatedEvent event = new TaskRunCreatedEvent(deployedTask.getWorkflowTaskId(), IdGenerator.getInstance().nextId());
        taskTimelineService.handleTaskRunCreatedEvent(event);

        List<TaskTimeline> taskTimelines = taskTimelineDao.fetchByDefinitionId(deployedTask.getDefinitionId());
        assertThat(taskTimelines.size(), is(1));
        TaskTimeline taskTimeline = taskTimelines.get(0);
        assertThat(taskTimeline.getId(), notNullValue());
        assertThat(taskTimeline.getTaskRunId(), is(event.getTaskRunId()));
        assertThat(taskTimeline.getDefinitionId(), is(deployedTask.getDefinitionId()));
        assertThat(taskTimeline.getLevel(), is(slaConfig.getLevel()));
        assertThat(taskTimeline.getDeadline(), is(slaConfig.getDeadline(deployedTask.getTaskCommit().getSnapshot().getTaskPayload().getScheduleConfig().getTimeZone())));
        assertThat(taskTimeline.getRootDefinitionId(), is(0L));
    }

    private void prepareTaskDefinitionWithOutBaseline(Long definitionId) {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition(definitionId);
        taskDefinitionDao.create(taskDefinition);
    }

    private void prepareTaskDefinitionWithBaseline(Long definitionId, SlaConfig slaConfig) {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition(definitionId, slaConfig);
        taskDefinitionDao.create(taskDefinition);
    }

    private void prepareDeployedTask(DeployedTask deployedTask) {
        deployedTaskDao.create(deployedTask);
        taskCommitDao.create(deployedTask.getTaskCommit());
    }

}
