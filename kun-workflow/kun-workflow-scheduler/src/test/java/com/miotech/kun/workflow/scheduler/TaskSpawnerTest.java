package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.variable.dao.VariableDao;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskRunEnv;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.testing.factory.MockVariableFactory;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.shazam.shazamcrest.matcher.CustomisableMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TaskSpawnerTest extends SchedulerTestBase {
    private static final String CRON_EVERY_MINUTE = "0 * * ? * * *";
    private static final String CRON_EVERY_THREE_MINUTE = "0 */3 * ? * * *";

    private static <T> CustomisableMatcher<T> safeSameBeanAs(T expected) {
        return sameBeanAs(expected)
                .ignoring(TaskDao.class)
                .ignoring(TaskRunDao.class)
                .ignoring(TaskRunDao.TaskRunMapper.class)
                .ignoring(DatabaseOperator.class)
                .ignoring("dependencyFunc");
    }

    @Inject
    private TaskSpawner taskSpawner;

    @Inject
    private EventBus eventBus;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private VariableDao variableDao;

    @Inject
    private TaskManager taskManager;

    private Long operatorId;

    @Override
    protected void configuration() {
        super.configuration();
        mock(TaskManager.class);
    }

    @Before
    public void initOperator() {
        operatorId = 1L;
        String className = "TestOperator1";

        String packagePath = OperatorCompiler.compileJar(TestOperator1.class, className);
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName(className)
                .withClassName(className)
                .withPackagePath(packagePath)
                .build();
        operatorDao.createWithId(op, operatorId);
    }

    @After
    public void resetClock() {
        DateTimeUtils.resetClock();
    }

    @Test
    public void testRun_graph_of_single_task_without_variables() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(Config.EMPTY)
                .build();
        taskDao.create(task);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        OffsetDateTime now = DateTimeUtils.freeze();

        // process
        DirectTaskGraph graph = new DirectTaskGraph(task);
        taskSpawner.run(graph);

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getScheduledTick(), is(new Tick(now)));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        assertThat(submitted.getConfig().size(), is(2));
        assertThat(submitted.getConfig().getString("var1"), is("default1"));
        assertThat(submitted.getConfig().getString("var2"), is("default2"));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    @Test
    public void testRun_graph_of_single_task_with_configured_variables() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(new Config(ImmutableMap.of("var1", "val1")))
                .build();
        taskDao.create(task);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        OffsetDateTime now = DateTimeUtils.freeze();

        // process
        TaskRunEnv context = buildEnv(task.getId(), ImmutableMap.of("var1", "val1"));
        DirectTaskGraph graph = new DirectTaskGraph(task);
        taskSpawner.run(graph, context);

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getScheduledTick(), is(new Tick(now)));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        assertThat(submitted.getConfig().size(), is(2));
        assertThat(submitted.getConfig().getString("var1"), is("val1"));
        assertThat(submitted.getConfig().getString("var2"), is("default2"));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    @Test
    public void testRun_graph_of_single_task_with_config_has_variables() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(new Config(ImmutableMap.of("var1", "${test-name.val1 }")))
                .build();
        taskDao.create(task);
        variableDao.create(MockVariableFactory.createVariable()
                .cloneBuilder()
                .withNamespace("test-name")
                .withKey("val1")
                .withValue("val1")
                .build());

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        TaskRunEnv context = buildEnv(task.getId(), ImmutableMap.of());
        DirectTaskGraph graph = new DirectTaskGraph(task);
        taskSpawner.run(graph, context);

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getConfig().size(), is(2));
        assertThat(submitted.getConfig().getString("var1"), is("val1"));
        assertThat(submitted.getConfig().getString("var2"), is("default2"));
    }

    @Test
    public void testRun_graph_of_multiple_tasks_not_depends_on_each_other() {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(3, operatorId, "0>>1;1>>2;").stream()
                .map(t -> {
                    taskDao.create(t);
                    return t;
                })
                .collect(Collectors.toList());

        Task task1 = tasks.get(0);
        Task task3 = tasks.get(2);

        OffsetDateTime now = DateTimeUtils.freeze();
        Tick tick = new Tick(now);
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        DirectTaskGraph graph = new DirectTaskGraph(task1, task3);
        taskSpawner.run(graph);

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));

        // task1
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));
        assertThat(submitted.getDependentTaskRunIds(), hasSize(0));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));

        // task3
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));
        assertThat(submitted.getDependentTaskRunIds(), hasSize(0));

        saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    @Test
    public void testRun_graph_of_multiple_tasks_depends_on_each_other() {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(3, operatorId, "0>>1;1>>2;").stream()
                .map(t -> {
                    taskDao.create(t);
                    return t;
                })
                .collect(Collectors.toList());

        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);
        Task task3 = tasks.get(2);

        OffsetDateTime now = DateTimeUtils.freeze();
        Tick tick = new Tick(now);
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        DirectTaskGraph graph = new DirectTaskGraph(task1, task2, task3);
        taskSpawner.run(graph);

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(3));

        // task1
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(tasks.get(0).getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));
        assertThat(submitted.getDependentTaskRunIds(), hasSize(0));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));

        // task2
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(tasks.get(1).getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));
        assertThat(submitted.getDependentTaskRunIds(), hasSize(1));
        assertThat(submitted.getDependentTaskRunIds(), contains(result.get(0).getId()));

        saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        // TODO: non-passed yet @Josh Ouyang
        // assertThat(submitted, sameBeanAs(saved));

        // task3
        submitted = result.get(2);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(tasks.get(2).getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));
        assertThat(submitted.getDependentTaskRunIds(), hasSize(1));
        assertThat(submitted.getDependentTaskRunIds(), contains(result.get(1).getId()));

        saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        // TODO: non-passed yet @Josh Ouyang
        // assertThat(submitted, sameBeanAs(saved));
    }

    @Test
    public void testHandleTickEvent_multiple_graphs() {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasks(2, operatorId);
        DirectTaskGraph graph1 = new DirectTaskGraph(Lists.newArrayList(tasks.get(0)));
        DirectTaskGraph graph2 = new DirectTaskGraph(Lists.newArrayList(tasks.get(1)));

        // process
        taskSpawner.schedule(graph1);
        taskSpawner.schedule(graph2);

        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        eventBus.post(new TickEvent(new Tick(next)));

        // verify
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));
    }

    @Test
    public void testCreateTaskRuns_single_task() {
        // prepare
        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE))
                .build();
        taskDao.create(task);

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.schedule(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }


    @Test
    public void testCreateTaskRuns_single_task_scheduled_3min() {
        DateTimeUtils.freeze();
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_THREE_MINUTE))
                .build();
        taskDao.create(task);

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.schedule(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        // emit 3 tick event, one minute each
        final int executionTime = 4;
        for (int i = 0; i < executionTime; i ++) {
            OffsetDateTime current = DateTimeUtils.now().plusSeconds(60*i);
            Tick tick = new Tick(current);
            eventBus.post(new TickEvent(tick));
        }

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager, times(executionTime))
                .submit(captor.capture());

        List<List<TaskRun>> result = captor.getAllValues();
        assertThat(result.size(), is(executionTime));

        assertThat(result.stream().filter(x -> !x.isEmpty()).count(), is(1L));

        DateTimeUtils.resetClock();
    }
    
    @Test
    public void testCreateTaskRuns_multiple_tasks() {
        // prepare
        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasks(2, operatorId).stream().map(t -> {
            Task t2 = t.cloneBuilder()
                    .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE))
                    .build();
            taskDao.create(t2);
            return t2;
        }).collect(Collectors.toList());

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.schedule(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));

        // task1
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(tasks.get(0)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));

        // task2
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(tasks.get(1)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    @Test
    public void testCreateTaskRuns_task_has_upstreams() {
        // prepare
        OffsetDateTime next = OffsetDateTime.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1");

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
        taskSpawner.schedule(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(task2.getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
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
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1")
                .stream().map(t -> {
                    Task t2 = t.cloneBuilder()
                    .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE))
                    .build();
            taskDao.create(t2);
            return t2;
        }).collect(Collectors.toList());

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.schedule(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(next);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));

        // task1
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(tasks.get(0)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));

        // task2
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(tasks.get(1).getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(nullValue()));

        saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        // TODO: non-passed yet @Josh Ouyang
        // assertThat(submitted, sameBeanAs(saved));

        // taskRun1 >> taskRun2
        assertThat(submitted.getDependentTaskRunIds(), hasSize(1));
        assertThat(submitted.getDependentTaskRunIds(), contains(result.get(0).getId()));
    }

    private TaskRunEnv buildEnv(Long taskId, Map<String, Object> config) {
        TaskRunEnv.Builder builder = TaskRunEnv.newBuilder()
                .addConfig(taskId, config);
        return builder.build();
    }

    private boolean invoked() {
        return !Mockito.mockingDetails(taskManager).getInvocations().isEmpty();
    }
}
