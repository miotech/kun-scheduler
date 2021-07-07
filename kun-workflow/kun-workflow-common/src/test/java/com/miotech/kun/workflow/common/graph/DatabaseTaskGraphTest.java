package com.miotech.kun.workflow.common.graph;

import com.cronutils.model.Cron;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.TimeZoneEnum;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.utils.CronUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTaskGraphTest extends DatabaseTestBase {

    @Inject
    private TaskDao taskDao;

    private TaskRunDao taskRunDao;

    private TaskGraph taskGraph;

    @Inject
    private DataSource dataSource;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void setup() {
        taskGraph = injector.getInstance(DatabaseTaskGraph.class);
    }




    @Test
    public void taskTopoSortInSameTick() {
        String cronExpression = "0 0 10 * * ?";
        Cron cron = CronUtils.convertStringToCron(cronExpression);
        Optional<OffsetDateTime> scheduleTime = CronUtils.getNextExecutionTimeFromNow(cron);
        Tick tick = new Tick(scheduleTime.get());
        ScheduleConf conf = new ScheduleConf(ScheduleType.SCHEDULED, cronExpression,TimeZoneEnum.UTC);
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, WorkflowIdGenerator.nextOperatorId(), "1>>2;0>>1", conf);
        saveTaskList(taskList);
        List<Long> expectTaskIds = taskList.stream().sorted(Comparator.comparing(Task::getId)).map(Task::getId).collect(Collectors.toList());
        List<Long> sortTaskIds = taskGraph.tasksScheduledAt(tick).stream().map(Task::getId).collect(Collectors.toList());
        assertEquals(expectTaskIds, sortTaskIds);

    }

    @Test
    //依赖的任务不在同一个调度的tick内
    public void taskTopoSortInDiffTick() {
        String upStreamTaskCron = "0 0 9 * * ?";
        Cron upStreamCron = CronUtils.convertStringToCron(upStreamTaskCron);
        Task upTask = MockTaskFactory.createTaskWithUpstreams(new ArrayList<>(),new ScheduleConf(ScheduleType.SCHEDULED, upStreamTaskCron, TimeZoneEnum.UTC) );
        String cronExpression = "0 0 10 * * ?";
        Cron cron = CronUtils.convertStringToCron(cronExpression);
        Optional<OffsetDateTime> preScheduleTime = CronUtils.getNextExecutionTimeFromNow(upStreamCron);
        Optional<OffsetDateTime> scheduleTime = CronUtils.getNextExecutionTimeFromNow(cron);
        ScheduleConf conf = new ScheduleConf(ScheduleType.SCHEDULED, cronExpression,TimeZoneEnum.UTC);
        Task tickHead = MockTaskFactory.createTaskWithUpstreams(Lists.newArrayList(upTask.getId()), conf);
        List<Task> tickMid = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            tickMid.add(MockTaskFactory.createTaskWithUpstreams(Lists.newArrayList(tickHead.getId()), conf));
        }
        List<Long> tickMidTaskIds = tickMid.stream().map(Task::getId).collect(Collectors.toList());
        Task tickTail = MockTaskFactory.createTaskWithUpstreams(tickMidTaskIds, conf);
        List<Task> tickTasks = new ArrayList<>();
        tickTasks.addAll(tickMid);
        tickTasks.add(tickHead);
        tickTasks.add(tickTail);
        saveTaskList(tickTasks);
        taskDao.create(upTask);
        Tick preTick = new Tick(preScheduleTime.get());
        Task preTask = taskGraph.tasksScheduledAt(preTick).get(0);
        taskGraph.updateTasksNextExecutionTick(preTick,Lists.newArrayList(preTask));

        assertThat(preTask.getId(),is(upTask.getId()));
        Tick tick = new Tick(scheduleTime.get());

        List<Long> sortTaskIds = taskGraph.tasksScheduledAt(tick).stream().map(Task::getId).collect(Collectors.toList());
        assertThat(sortTaskIds.get(0),is(tickHead.getId()));
        assertArrayEquals(sortTaskIds.subList(1,3).toArray(),tickMidTaskIds.toArray());
        assertThat(sortTaskIds.get(3),is(tickTail.getId()));
    }

    @Test
    //任务依赖存在环
    public void taskHasCycle(){
        String cronExpression = "0 0 10 * * ?";
        Cron cron = CronUtils.convertStringToCron(cronExpression);
        Optional<OffsetDateTime> scheduleTime = CronUtils.getNextExecutionTimeFromNow(cron);
        Tick tick = new Tick(scheduleTime.get());
        ScheduleConf conf = new ScheduleConf(ScheduleType.SCHEDULED, cronExpression,TimeZoneEnum.UTC);
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, WorkflowIdGenerator.nextOperatorId(), "1>>2;0>>1;2>>0", conf);
        saveTaskList(taskList);
        thrown.expectMessage("has cycle in task dependencies");
        List<Long> sortTaskIds = taskGraph.tasksScheduledAt(tick).stream().map(Task::getId).collect(Collectors.toList());

    }

    @Test
    //任务有多个上下游
    public void taskTopoSortHasMultiUpOrDownStream() {
        String cronExpression = "0 0 10 * * ?";
        Cron cron = CronUtils.convertStringToCron(cronExpression);
        Optional<OffsetDateTime> scheduleTime = CronUtils.getNextExecutionTimeFromNow(cron);
        Tick tick = new Tick(scheduleTime.get());
        ScheduleConf conf = new ScheduleConf(ScheduleType.SCHEDULED, cronExpression,TimeZoneEnum.UTC);
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(6, WorkflowIdGenerator.nextOperatorId(), "2>>1;2>>3;2>>4;1>>5;1>>0", conf);
        saveTaskList(taskList);
        List<Long> createTaskIds = taskList.stream().map(Task::getId).collect(Collectors.toList());
        List<Long> lastTaskIds = Lists.newArrayList(createTaskIds.get(0), createTaskIds.get(5));
        List<Long> midTaskIds = Lists.newArrayList(createTaskIds.get(1), createTaskIds.get(3), createTaskIds.get(4));
        List<Long> firstIds = Lists.newArrayList(createTaskIds.get(2));
        List<Long> sortTaskIds = taskGraph.tasksScheduledAt(tick).stream().map(Task::getId).collect(Collectors.toList());
        assertThat(sortTaskIds.get(0), is(firstIds.get(0)));
        assertArrayEquals(sortTaskIds.subList(1, 4).toArray(), midTaskIds.toArray());
        assertArrayEquals(sortTaskIds.subList(4, 6).toArray(), (lastTaskIds).toArray());

    }


    private void saveTaskList(List<Task> taskList) {
        for (Task task : taskList) {
            taskDao.create(task);
        }
    }


}
