package com.miotech.kun.monitor.sla.timeline.service;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.ScheduleConfig;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.web.common.deploy.dao.DeployedTaskDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.sla.AppTestBase;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.common.service.SlaService;
import com.miotech.kun.monitor.sla.common.service.TaskTimelineService;
import com.miotech.kun.monitor.sla.mocking.*;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

    @Autowired
    private SlaService slaService;

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
        assertThat(taskTimeline.getRootDefinitionId(), nullValue());
    }

    @Test
    public void testHandleTaskRunCreatedEvent_backtracking() {
        DeployedTask deployedTask1 = MockDeployedTaskFactory.createDeployedTask();
        prepareDeployedTask(deployedTask1);
        DeployedTask deployedTask2 = MockDeployedTaskFactory.createDeployedTask();
        prepareDeployedTask(deployedTask2);

        ScheduleConfig scheduleConfig1 = deployedTask1.getTaskCommit().getSnapshot().getTaskPayload().getScheduleConfig();
        SlaConfig slaConfig1 = deployedTask1.getTaskCommit().getSnapshot().getTaskPayload().getScheduleConfig().getSlaConfig();
        ScheduleConfig scheduleConfig2 = deployedTask2.getTaskCommit().getSnapshot().getTaskPayload().getScheduleConfig();
        SlaConfig slaConfig2 = deployedTask2.getTaskCommit().getSnapshot().getTaskPayload().getScheduleConfig().getSlaConfig();
        TaskRunCreatedEvent event = new TaskRunCreatedEvent(deployedTask1.getWorkflowTaskId(), IdGenerator.getInstance().nextId());

        // build data in neo4j
        int task2Runtime = 10;
        slaService.save(TaskDefinitionNode.from(deployedTask1.getDefinitionId(), deployedTask1.getName(),
                slaConfig1.getLevel(), slaConfig1.getDeadlineValue(scheduleConfig1.getTimeZone()), deployedTask1.getWorkflowTaskId(), null));
        slaService.save(TaskDefinitionNode.from(deployedTask2.getDefinitionId(), deployedTask2.getName(),
                slaConfig2.getLevel(), slaConfig2.getDeadlineValue(scheduleConfig2.getTimeZone()), deployedTask2.getWorkflowTaskId(), task2Runtime));
        slaService.bind(deployedTask1.getDefinitionId(), deployedTask2.getDefinitionId(), TaskDefinitionNode.Relationship.OUTPUT);

        taskTimelineService.handleTaskRunCreatedEvent(event);
        List<TaskTimeline> taskTimelines = taskTimelineDao.fetchByDefinitionId(deployedTask1.getDefinitionId());
        assertThat(taskTimelines.size(), is(2));
        TaskTimeline taskTimeline1 = taskTimelines.get(0);
        TaskTimeline taskTimeline2 = taskTimelines.get(1);
        assertThat(taskTimeline1.getRootDefinitionId(), is(deployedTask2.getDefinitionId()));
        assertThat(taskTimeline2.getDeadline(), is(slaConfig1.getDeadline(scheduleConfig1.getTimeZone())));

        int value = slaConfig1.getHours() * 60 + slaConfig1.getMinutes() - task2Runtime;
        SlaConfig slaConfig = new SlaConfig(null, value / 60, value % 60, null);
        assertThat(taskTimeline1.getDeadline(), is(slaConfig.getDeadline(scheduleConfig1.getTimeZone())));

    }

    private void prepareTaskDefinitionWithOutBaseline(Long definitionId) {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition(definitionId);
        taskDefinitionDao.create(taskDefinition);
    }

    private void prepareDeployedTask(DeployedTask deployedTask) {
        deployedTaskDao.create(deployedTask);
        taskCommitDao.create(deployedTask.getTaskCommit());
    }

}
