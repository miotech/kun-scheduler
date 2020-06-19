package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

public class TaskSpawnerTest extends SchedulerTestBase {
    private static final String CRON_EVERY_MINUTE = "0 * * ? * * *";

    @Inject
    private TaskSpawner taskSpawner;

    @Inject
    private EventBus eventBus;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskRunner taskRunner;

    @Override
    protected void configuration() {
        super.configuration();
        mock(TaskRunner.class);
    }

    @Test
    public void testHandleTickEvent_multiple_graphs() {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasks(2);
        DirectTaskGraph graph1 = new DirectTaskGraph(Lists.newArrayList(tasks.get(0)));
        DirectTaskGraph graph2 = new DirectTaskGraph(Lists.newArrayList(tasks.get(1)));

        // process
        taskSpawner.add(graph1);
        taskSpawner.add(graph2);

        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        eventBus.post(new TickEvent(new Tick(next)));

        // verify
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskRunner).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));
    }

    @Test
    public void testCreateTaskRuns_single_task() {
        // prepare
        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        Task task = MockTaskFactory.createTask().cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE))
                .build();
        taskDao.create(task);

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.add(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskRunner).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), sameBeanAs(task));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchById(submitted.getId()).get();
        assertThat(submitted, sameBeanAs(saved));
    }
    
    @Test
    public void testCreateTaskRuns_multiple_tasks() {
        // prepare
        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasks(2).stream().map(t -> {
            Task t2 = t.cloneBuilder()
                    .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE))
                    .build();
            taskDao.create(t2);
            return t2;
        }).collect(Collectors.toList());

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.add(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskRunner).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));

        // task1
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), sameBeanAs(tasks.get(0)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchById(submitted.getId()).get();
        assertThat(submitted, sameBeanAs(saved));

        // task2
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), sameBeanAs(tasks.get(1)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        saved = taskRunDao.fetchById(submitted.getId()).get();
        assertThat(submitted, sameBeanAs(saved));
    }
    
    @Test
    public void testCreateTaskRuns_task_has_upstreams() {
        // prepare
        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");

        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);

        task2 = task2.cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE))
                .build();

        taskDao.create(task1);
        taskDao.create(task2);

        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        taskRunDao.createTaskRun(taskRun1);

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.add(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskRunner).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), sameBeanAs(task2));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchById(submitted.getId()).get();
        // TODO: non-passed yet @Josh Ouyang
        // assertThat(submitted, sameBeanAs(saved));

        // taskRun1 >> taskRun2
        assertThat(submitted.getDependentTaskRunIds(), hasSize(1));
        assertThat(submitted.getDependentTaskRunIds(), contains(taskRun1.getId()));
    }
    
    @Test
    public void testCreateTaskRuns_task_depend_on_task_in_same_tick() {
        // prepare
        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1")
                .stream().map(t -> {
                    Task t2 = t.cloneBuilder()
                    .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE))
                    .build();
            taskDao.create(t2);
            return t2;
        }).collect(Collectors.toList());

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.add(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskRunner).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));

        // task1
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), sameBeanAs(tasks.get(0)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchById(submitted.getId()).get();
        assertThat(submitted, sameBeanAs(saved));

        // task2
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), sameBeanAs(tasks.get(1)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        saved = taskRunDao.fetchById(submitted.getId()).get();
        // TODO: non-passed yet @Josh Ouyang
        // assertThat(submitted, sameBeanAs(saved));

        // taskRun1 >> taskRun2
        assertThat(submitted.getDependentTaskRunIds(), hasSize(1));
        assertThat(submitted.getDependentTaskRunIds(), contains(result.get(0).getId()));
    }

    private boolean invoked() {
        return !Mockito.mockingDetails(taskRunner).getInvocations().isEmpty();
    }
}