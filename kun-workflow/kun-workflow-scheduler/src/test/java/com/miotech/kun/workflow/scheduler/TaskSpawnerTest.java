package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.workflow.common.executetarget.ExecuteTargetService;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.tick.TickDao;
import com.miotech.kun.workflow.common.variable.dao.VariableDao;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.SpecialTick;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.*;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.testing.factory.MockVariableFactory;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.CronUtils;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.shazam.shazamcrest.matcher.CustomisableMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class TaskSpawnerTest extends SchedulerTestBase {
    private static final String CRON_EVERY_MINUTE = "0 * * ? * * *";
    private static final String CRON_EVERY_DAY = "0 0 1 ? * * *";
    private static final String CRON_EVERY_THREE_MINUTE = "0 */3 * ? * * *";

    private static <T> CustomisableMatcher<T> safeSameBeanAs(T expected) {
        return sameBeanAs(expected)
                .ignoring("createdAt")
                .ignoring("updatedAt")
                .ignoring("startAt")
                .ignoring("endAt")
                .ignoring("taskRunConditions")
                .ignoring(TaskDao.class)
                .ignoring(TaskRunDao.class)
                .ignoring(TaskRunDao.TaskRunMapper.class)
                .ignoring(DatabaseOperator.class)
                .ignoring(DependencyFunction.class);
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

    @Inject
    private TickDao tickDao;

    @Inject
    private SchedulerClock schedulerClock;

    @Inject
    private DatabaseOperator databaseOperator;

    private ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender;

    private Long operatorId;

    @Inject
    private ExecuteTargetService executeTargetService;

    @Override
    protected void configuration() {
        super.configuration();
        mock(TaskManager.class);
    }

    @BeforeEach
    public void init() {

        databaseOperator.update("truncate table kun_wf_target RESTART IDENTITY");

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
        appender = mock(ch.qos.logback.core.Appender.class);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TaskSpawner.class)).addAppender(appender);
    }

    @AfterEach
    public void resetClock() {
        DateTimeUtils.resetClock();
    }

    @Test
    public void taskRunDependencyCreateFailed() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, operatorId, "0>>1;1>>2");
        Task task2 = taskList.remove(1);
        Config config = Config.newBuilder()
                .addConfig("var1", new ArrayList<>()).build();
        Task errorTask2 = task2.cloneBuilder().withConfig(config).build();
        taskList.add(1, errorTask2);
        for (Task task : taskList) {
            taskDao.create(task);
        }
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);


        // process
        DirectTaskGraph graph = new DirectTaskGraph(taskList);

        ArgumentCaptor<ch.qos.logback.classic.spi.ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);

        taskSpawner.run(graph);


        //verify log
        verify(appender, atLeast(0)).doAppend(logCaptor.capture());
        String logs = logCaptor.getAllValues().stream().map(log -> log.getMessage()).collect(Collectors.joining(";"));
        assertThat(logs, containsString("create taskRun failed , taskId = {}"));


        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());
        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(2));

        TaskRun submitted1 = result.get(0);
        assertThat(submitted1.getId(), is(notNullValue()));
        assertThat(submitted1.getTask(), safeSameBeanAs(taskList.get(0)));
        assertThat(submitted1.getScheduledTick(), is(SpecialTick.NULL.toTick()));
        assertThat(submitted1.getStartAt(), is(nullValue()));
        assertThat(submitted1.getEndAt(), is(nullValue()));
        assertThat(submitted1.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted1.getConfig().size(), is(2));
        assertThat(submitted1.getConfig().getString("var1"), is("default1"));
        assertThat(submitted1.getConfig().getString("var2"), is("default2"));

        TaskRun saved1 = taskRunDao.fetchTaskRunById(submitted1.getId()).get();
        assertThat(submitted1, safeSameBeanAs(saved1));

        TaskRun submitted2 = result.get(1);
        assertThat(submitted2.getId(), is(notNullValue()));
        assertThat(submitted2.getScheduledTick(), is(SpecialTick.NULL.toTick()));
        assertThat(submitted2.getStartAt(), is(nullValue()));
        assertThat(submitted2.getEndAt(), is(nullValue()));
        assertThat(submitted2.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted2.getConfig().size(), is(2));
        assertThat(submitted2.getConfig().getString("var1"), is("default1"));
        assertThat(submitted2.getConfig().getString("var2"), is("default2"));
    }

    @Test
    public void taskRunCreateFailed() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(Config.EMPTY)
                .build();
        taskDao.create(task);
        Config config = Config.newBuilder()
                .addConfig("var1", new ArrayList<>()).build();
        Task errorTask = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(config)
                .build();
        taskDao.create(errorTask);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        OffsetDateTime now = DateTimeUtils.freeze();


        // process
        DirectTaskGraph graph = new DirectTaskGraph(task, errorTask);

        ArgumentCaptor<ch.qos.logback.classic.spi.ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);

        taskSpawner.run(graph);

        //verify log
        verify(appender, atLeast(0)).doAppend(logCaptor.capture());
        String logs = logCaptor.getAllValues().stream().map(log -> log.getMessage()).collect(Collectors.joining(";"));
        assertThat(logs, containsString("create taskRun failed , taskId = {}"));


        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());
        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getScheduledTick(), is(SpecialTick.NULL.toTick()));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted.getConfig().size(), is(2));
        assertThat(submitted.getConfig().getString("var1"), is("default1"));
        assertThat(submitted.getConfig().getString("var2"), is("default2"));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }


    @Test
    public void testRun_graph_of_single_task_without_variables() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(Config.EMPTY)
                .build();
        taskDao.create(task);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

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
        assertThat(submitted.getScheduledTick(), is(SpecialTick.NULL.toTick()));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted.getConfig().size(), is(2));
        assertThat(submitted.getConfig().getString("var1"), is("default1"));
        assertThat(submitted.getConfig().getString("var2"), is("default2"));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    private Task prepareTask() {
        ScheduleConf scheduleConf = new ScheduleConf(ScheduleType.SCHEDULED, "0 */1 * * * ?", ZoneOffset.UTC.getId());
        return MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(Config.EMPTY)
                .withRecoverTimes(1)
                .withScheduleConf(scheduleConf)
                .build();
    }

    @Test
    public void restartAfterTaskRunSave() {
        Tick tick = new Tick(DateTimeUtils.now());
        Tick checkPoint = new Tick(tick.toOffsetDateTime().plusMinutes(-1));
        tickDao.saveCheckPoint(checkPoint);
        Task task = prepareTask();
        taskDao.create(task);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRuns(Arrays.asList(taskRun));
        //taskSpawner restart
        taskSpawner.init();
        schedulerClock.start(checkPoint.toOffsetDateTime());
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
        DatabaseTaskGraph graph = new DatabaseTaskGraph(taskDao);
        List<TaskRun> taskRunList = taskSpawner.run(graph, tick);
        assertThat(taskRunList.size(), is(0));
        List<Long> unStartedTaskRunIdList = taskRunDao.fetchTaskRunListWithoutAttempt()
                .stream().map(TaskRun::getId).collect(Collectors.toList());
        assertThat(unStartedTaskRunIdList, containsInAnyOrder(taskRun.getId()));
    }

    @Test
    public void restartAfterUpdateGraphSave() {
        Tick tick = new Tick(DateTimeUtils.now());
        Tick checkPoint = new Tick(tick.toOffsetDateTime().plusMinutes(-1));
        tickDao.saveCheckPoint(checkPoint);
        Task task = prepareTask();
        taskDao.create(task);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRuns(Arrays.asList(taskRun));
        DatabaseTaskGraph graph = new DatabaseTaskGraph(taskDao);
        graph.updateTasksNextExecutionTick(tick, Arrays.asList(task));
        //taskSpawner restart
        taskSpawner.init();
        schedulerClock.start(checkPoint.toOffsetDateTime());
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
        Optional<Tick> beforeRunTickOptional = taskDao.fetchNextExecutionTickByTaskId(task.getId());
        List<TaskRun> taskRunList = taskSpawner.run(graph, tick);
        assertThat(taskRunList.size(), is(0));
        Optional<Tick> nextTickOptional = taskDao.fetchNextExecutionTickByTaskId(task.getId());
        assertEquals(beforeRunTickOptional.get(), nextTickOptional.get());
    }

    @Test
    public void restartAfterCheckPointSave() {
        Tick tick = new Tick(DateTimeUtils.now());
        Tick checkPoint = new Tick(tick.toOffsetDateTime().plusMinutes(-1));
        tickDao.saveCheckPoint(checkPoint);
        Task task = prepareTask();
        taskDao.create(task);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRuns(Arrays.asList(taskRun));
        DatabaseTaskGraph graph = new DatabaseTaskGraph(taskDao);
        graph.updateTasksNextExecutionTick(tick, Arrays.asList(task));
        tickDao.saveCheckPoint(tick);
        //taskSpawner restart
        taskSpawner.init();
        schedulerClock.start(checkPoint.toOffsetDateTime());
        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
        Tick nextTick = new Tick(tick.toOffsetDateTime().plusMinutes(1));
        List<TaskRun> taskRunList = taskSpawner.run(graph, nextTick);
        assertThat(taskRunList.size(), is(1));
        Optional<OffsetDateTime> expectNextScheduleTimeOptional = CronUtils.getNextUTCExecutionTimeByExpr(task.getScheduleConf().getCronExpr(),
                nextTick.toOffsetDateTime(), "UTC");
        Optional<Tick> nextTickOptional = taskDao.fetchNextExecutionTickByTaskId(task.getId());
        assertEquals(expectNextScheduleTimeOptional.get(), nextTickOptional.get().toOffsetDateTime());
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
        assertThat(submitted.getScheduledTick(), is(SpecialTick.NULL.toTick()));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

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

        Tick tick = SpecialTick.NULL.toTick();
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(submitted.getDependentTaskRunIds(), hasSize(0));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));

        // task3
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
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

        Tick tick = SpecialTick.NULL.toTick();
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
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

        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
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
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId()))
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }


    @Test
    public void testCreateTaskRuns_single_task_scheduled_3min() {
        DateTimeUtils.freeze();
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_THREE_MINUTE, ZoneOffset.UTC.getId()))
                .build();
        taskDao.create(task);

        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.schedule(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        // emit 3 tick event, one minute each
        final int executionTime = 4;
        for (int i = 0; i < executionTime; i++) {
            OffsetDateTime current = DateTimeUtils.now().plusSeconds(60 * i);
            Tick tick = new Tick(current);
            eventBus.post(new TickEvent(tick));
        }

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager, times(1))
                .submit(captor.capture());

        List<List<TaskRun>> result = captor.getAllValues();
        assertThat(result.size(), is(1));

        assertThat(result.stream().filter(x -> !x.isEmpty()).count(), is(1L));

        DateTimeUtils.resetClock();
    }

    @Test
    public void testCreateTaskRuns_multiple_tasks() {
        // prepare
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasks(2, operatorId).stream().map(t -> {
            Task t2 = t.cloneBuilder()
                    .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId()))
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));

        // task2
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(tasks.get(1)));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    @Test
    public void testCreateTaskRuns_task_has_upstreams() {
        // prepare
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1");

        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);

        task2 = task2.cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId()))
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

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
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1")
                .stream().map(t -> {
                    Task t2 = t.cloneBuilder()
                            .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId()))
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
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));

        // task2
        submitted = result.get(1);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(tasks.get(1).getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        // TODO: non-passed yet @Josh Ouyang
        // assertThat(submitted, sameBeanAs(saved));

        // taskRun1 >> taskRun2
        assertThat(submitted.getDependentTaskRunIds(), hasSize(1));
        assertThat(submitted.getDependentTaskRunIds(), contains(result.get(0).getId()));
    }

    @Test
    public void repeatRunTaskInOneMinute() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withConfig(Config.EMPTY)
                .build();
        taskDao.create(task);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        Tick tick = SpecialTick.NULL.toTick();
        List<TaskRun> submitTaskRuns = new ArrayList<>();
        // process
        for (int i = 0; i < 5; i++) {
            DirectTaskGraph graph = new DirectTaskGraph(task);
            submitTaskRuns.addAll(taskSpawner.run(graph));
        }

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager, times(5)).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));
        Set<Long> taskRunIdSet = new HashSet<>();
        for (TaskRun submitted : submitTaskRuns) {
            assertThat(submitted.getId(), is(notNullValue()));
            assertThat(submitted.getTask().getId(), is(task.getId()));
            assertThat(submitted.getScheduledTick(), is(tick));
            assertThat(submitted.getStartAt(), is(nullValue()));
            assertThat(submitted.getEndAt(), is(nullValue()));
            assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));
            taskRunIdSet.add(submitted.getId());
        }
        assertThat(taskRunIdSet, hasSize(5));
    }

    @Test
    public void testScheduleTaskRunUpstreamIsFailed() {
        // prepare
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1");

        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        taskDao.create(task1);
        taskDao.create(task2);
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

        // task2
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(tasks.get(1).getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted.getDependentTaskRunIds(), hasSize(1));
        assertThat(submitted.getDependentTaskRunIds(), contains(taskRun1.getId()));
    }

    @Test
    //scenario: TaskRun 0>>1
    //         before 1 create, 0 failed
    //        when 1 create, 0 is added to failedUpstreamTaskRunIds of 1
    public void singleUpstreamFailedBeforeCreate_updateSuccess() {
        // prepare
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1");
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        taskDao.create(task1);
        taskDao.create(task2);
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

        // task2
        TaskRun taskRun2 = result.get(0);
        assertThat(taskRun2.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskRun2.getFailedUpstreamTaskRunIds().size(), is(1));
        assertThat(taskRun2.getFailedUpstreamTaskRunIds().get(0), is(taskRun1.getId()));
    }

    @Test
    // scenario: TaskRun 0>>1; 0>>2; 1,2>>3
    //         before 3 create, 0 failed
    //          when 3 create, 0 is added to failedUp of 3 by 1 and by 2 without duplicate
    public void mutualUpstreamFailedBeforeCreate_updateTaskRunsWithoutDuplicate_Success() {
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(4, operatorId, "0>>1;0>>2;1>>3;2>>3");
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        Task task3 = tasks.get(2).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        Task task4 = tasks.get(3).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);
        taskDao.create(task4);
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
        assertThat(result.size(), is(3));

        TaskRun taskRun4 = result.get(2);
        assertThat(taskRun4.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskRun4.getFailedUpstreamTaskRunIds().size(), is(1));
    }

    @Test
    //scenario: TaskRun 0>>1; 1>>3; 2>>3
    //         before 3 create, 0 & 2 failed
    //        when 3 create, 0 (not 1) & 2 is added to failedUpstreamTaskRunIds of 3
    public void multiUpstreamFailedBeforeCreate_updateSuccess() {
        // prepare
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(4, operatorId, "0>>1;1>>3;2>>3");
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        Task task3 = tasks.get(2);
        Task task4 = tasks.get(3).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3)
                .cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);
        taskDao.create(task4);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun3);

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

        TaskRun taskRun4 = result.get(1);

        assertThat(taskRun4.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskRun4.getFailedUpstreamTaskRunIds().size(), is(2));
        assertThat(new HashSet<>(taskRun4.getFailedUpstreamTaskRunIds()),
                is(new HashSet<>(Arrays.asList(taskRun1.getId(), taskRun3.getId()))));
    }

    @Test
    public void testTaskRUnUpStreamNotFound_ShouldCreate() {
        // prepare
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1");

        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId())).build();
        taskDao.create(task1);
        taskDao.create(task2);

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

        // task2
        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask().getId(), is(tasks.get(1).getId()));
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted.getDependentTaskRunIds(), hasSize(0));
    }

    @Test
    public void testTaskRunUpstreamCrossDays_ShouldCreate() {
        // prepare
        DateTimeUtils.freezeAt("202001012000");
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, operatorId, "0>>1");

        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_DAY, ZoneOffset.UTC.getId())).build();
        taskDao.create(task1);
        taskDao.create(task2);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        taskRunDao.createTaskRun(taskRun1);

        //two days after
        OffsetDateTime twoDaysAfter = DateTimeUtils.freezeAt("202001020101");
        DatabaseTaskGraph graph = injector.getInstance(DatabaseTaskGraph.class);
        taskSpawner.schedule(graph);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);

        // process
        Tick tick = new Tick(twoDaysAfter);
        eventBus.post(new TickEvent(tick));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));
    }

    @Test
    public void testCreateTaskRuns_should_send_event() {
        // collect taskRun create events
        TaskRunEventCreatedEventListener listener = new TaskRunEventCreatedEventListener();
        eventBus.register(listener);


        // prepare
        OffsetDateTime next = DateTimeUtils.now().plusSeconds(120);
        Task task1 = MockTaskFactory.createTask(operatorId).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId()))
                .build();;


        taskDao.create(task1);


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
        assertThat(submitted.getScheduledTick(), is(tick));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        TaskRunCreatedEvent event = (TaskRunCreatedEvent) listener.getLastEvent().get();
        assertThat(event.getTaskId(), is(task1.getId()));
        assertThat(event.getTaskRunId(), is(submitted.getId()));

    }


    @Test
    public void runTaskWithTarget_should_contains_in_taskRun() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId);
        taskDao.create(task);
        //prepare
        ExecuteTarget testTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(testTarget);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        // process
        TaskRunEnv context = buildEnv(task.getId(), ImmutableMap.of("var1", "val1"),1l);
        DirectTaskGraph graph = new DirectTaskGraph(task);
        taskSpawner.run(graph, context);

        // verify
        ExecuteTarget expectTarget = executeTargetService.fetchExecuteTarget(1l);
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getScheduledTick(), is(SpecialTick.NULL.toTick()));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted.getConfig().size(), is(2));
        assertThat(submitted.getConfig().getString("var1"), is("val1"));
        assertThat(submitted.getConfig().getString("var2"), is("default2"));
        assertThat(submitted.getExecuteTarget(),is(expectTarget));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    @Test
    public void runTaskWithoutTarget_should_use_defaultTarget_taskRun() {
        // prepare
        Task task = MockTaskFactory.createTask(operatorId);
        taskDao.create(task);
        //prepare
        ExecuteTarget testTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(testTarget);
        //prepare
        ExecuteTarget prodTarget = ExecuteTarget.newBuilder()
                .withName("prod")
                .build();
        executeTargetService.createExecuteTarget(prodTarget);

        ArgumentCaptor<List<TaskRun>> captor = ArgumentCaptor.forClass(List.class);
        // process
        TaskRunEnv context = buildEnv(task.getId(), ImmutableMap.of("var1", "val1"),null);
        DirectTaskGraph graph = new DirectTaskGraph(task);
        taskSpawner.run(graph, context);

        // verify
        ExecuteTarget defaultTarget = executeTargetService.getDefaultTarget();
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(taskManager).submit(captor.capture());

        List<TaskRun> result = captor.getValue();
        assertThat(result.size(), is(1));

        TaskRun submitted = result.get(0);
        assertThat(submitted.getId(), is(notNullValue()));
        assertThat(submitted.getTask(), safeSameBeanAs(task));
        assertThat(submitted.getScheduledTick(), is(SpecialTick.NULL.toTick()));
        assertThat(submitted.getStartAt(), is(nullValue()));
        assertThat(submitted.getEndAt(), is(nullValue()));
        assertThat(submitted.getStatus(), is(TaskRunStatus.CREATED));

        assertThat(submitted.getConfig().size(), is(2));
        assertThat(submitted.getConfig().getString("var1"), is("val1"));
        assertThat(submitted.getConfig().getString("var2"), is("default2"));
        assertThat(submitted.getExecuteTarget(),is(defaultTarget));

        TaskRun saved = taskRunDao.fetchTaskRunById(submitted.getId()).get();
        assertThat(submitted, safeSameBeanAs(saved));
    }

    class TaskRunEventCreatedEventListener extends EventCollector {

        @Subscribe
        @Override
        public void onReceive(Event event) {
            if (event instanceof TaskRunCreatedEvent) {
                events.add(event);
            }
        }
    }


    private TaskRunEnv buildEnv(Long taskId, Map<String, Object> config) {
        TaskRunEnv.Builder builder = TaskRunEnv.newBuilder()
                .addConfig(taskId, config);
        return builder.build();
    }

    private TaskRunEnv buildEnv(Long taskId, Map<String, Object> config,Long targetId) {
        TaskRunEnv.Builder builder = TaskRunEnv.newBuilder()
                .addConfig(taskId, config)
                .setTargetId(targetId);
        return builder.build();
    }

    private boolean invoked() {
        return !Mockito.mockingDetails(taskManager).getInvocations().isEmpty();
    }
}
