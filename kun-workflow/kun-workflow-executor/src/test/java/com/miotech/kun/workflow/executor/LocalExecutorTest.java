package com.miotech.kun.workflow.executor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.TaskRunStateMachine;
import com.miotech.kun.workflow.common.executetarget.ExecuteTargetService;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.*;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.CheckType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceKind;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.executor.local.*;
import com.miotech.kun.workflow.executor.mock.*;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.ResourceUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.joor.Reflect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doAnswer;

public class LocalExecutorTest extends CommonTestBase {

    private Executor executor;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private ResourceLoader resourceLoader;

    @Inject
    private EventBus eventBus;

    @Inject
    private MiscService miscService;

    private LocalProcessBackend spyBackend;

    private LocalQueueManage localQueueManager;

    @Inject
    private DataSource dataSource;

    private LocalProcessMonitor workerMonitor;

    private WorkerLifeCycleManager workerLifeCycleManager;

    @Inject
    private ExecuteTargetService executeTargetService;

    @Inject
    private DatabaseOperator databaseOperator;

    @Inject
    private TaskRunStateMachine taskRunStateMachine;

    private MockEventSubscriber mockEventSubscriber;

    @Inject
    private Injector injector;


    private Props props;

    private final Logger logger = LoggerFactory.getLogger(LocalExecutorTest.class);

    public TemporaryFolder tempFolder = new TemporaryFolder();

    private EventCollector eventCollector;

    @Override
    protected void configuration() {
        props = new Props();
        props.put("datasource.maxPoolSize", 1);
        props.put("datasource.minimumIdle", 0);
        props.put("neo4j.uri", neo4jContainer.getBoltUrl());
        props.put("neo4j.username", "neo4j");
        props.put("neo4j.password", neo4jContainer.getAdminPassword());
        super.configuration();
        MetadataServiceFacade mockMetadataFacade = Mockito.mock(MetadataServiceFacade.class);
        bind(SessionFactory.class, mock(SessionFactory.class));
        bind(MetadataServiceFacade.class, mockMetadataFacade);
        bind(LineageServiceFacade.class, mock(LineageServiceFacade.class));
        bind(EventBus.class, new EventBus());
        bind(EventPublisher.class, new NopEventPublisher());
        mockEventSubscriber = new MockEventSubscriber();
        bind(EventSubscriber.class, mockEventSubscriber);
        LocalProcessBackend localProcessBackend = new LocalProcessBackend();
        spyBackend = spy(localProcessBackend);
        bind(Props.class, props);
    }

    @BeforeEach
    public void setUp() throws IOException {
        tempFolder.create();
        databaseOperator.update("truncate table kun_wf_target RESTART IDENTITY");
        props.put("datasource.jdbcUrl", postgres.getJdbcUrl() + "&stringtype=unspecified");
        props.put("datasource.username", postgres.getUsername());
        props.put("datasource.password", postgres.getPassword());
        props.put("datasource.driverClassName", "org.postgresql.Driver");

        ExecutorConfig executorConfig = new ExecutorConfig();
        ResourceQueue defaultQueue = ResourceQueue.newBuilder()
                .withQueueName("default")
                .withWorkerNumbers(2)
                .build();
        ResourceQueue testQueue = ResourceQueue.newBuilder()
                .withQueueName("test")
                .withWorkerNumbers(2)
                .build();

        Map<String, String> storageMap = new HashMap<>();
        storageMap.put("logDir", "/tmp/logs");
        storageMap.put("operatorDir", "/tmp/operators");
        storageMap.put("commandDir", "/tmp/commands");

        executorConfig.setResourceQueues(Lists.newArrayList(defaultQueue, testQueue));
        executorConfig.setStorage(storageMap);
        executorConfig.setKind("local");
        executorConfig.setName("local");
        executorConfig.setLabel("local");

        executor = new LocalExecutor(executorConfig);
        localQueueManager = new LocalQueueManage(executorConfig, spyBackend);
        Reflect.on(executor).set("localQueueManager", localQueueManager);
        workerMonitor = new LocalProcessMonitor(spyBackend);
        workerLifeCycleManager = new LocalProcessLifeCycleManager(executorConfig, workerMonitor, localQueueManager, spyBackend);
        Reflect.on(executor).set("processLifeCycleManager", workerLifeCycleManager);
        executor.injectMembers(injector);
        executor.init();

        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
        taskRunStateMachine.start();
    }

    @AfterEach
    @Override
    public void tearDown() {
        workerLifeCycleManager.shutdown();
        workerMonitor.stop();
        super.tearDown();
        try {
            ((HikariDataSource) dataSource).close();
        } catch (Exception e) {

        }

    }

    private static class NopEventPublisher implements EventPublisher {
        @Override
        public void publish(Event event) {
            // nop
        }
    }


    @Test
    //taskAttempt未下发到worker执行，executor重启
    public void executorRestartBeforeWorkerStart() throws IOException {
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(mockTaskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, attempt);
        doAnswer(invocation -> {
            //do noting to ensure worker not start
            return null;
        }).when(spyBackend).startProcess(ArgumentMatchers.any(), ArgumentMatchers.any());
        executor.submit(attempt);
        executor.reset();
        doAnswer(invocation ->
                invocation.callRealMethod()
        ).when(spyBackend).startProcess(ArgumentMatchers.any(), ArgumentMatchers.any());
        executor.recover();
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));
        assertThat(content, containsString("URLClassLoader"));
        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);
        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));

    }


    @Test
    //任务下发到worker执行后executor重启
    public void executorRestartAfterWorkerStarted() throws IOException {
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(mockTaskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, attempt);
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());
        executor.reset();
        executor.recover();

        awaitUntilAttemptDone(attempt.getId());

        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));
        assertThat(content, containsString("URLClassLoader"));
        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);

        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));

        awaitUntilProcessDown("default", 0);
        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));

    }

    @Test
    //taskAttempt下发到worker执行，executor重启,重启前销毁worker
    public void executorRestartWhenWorkerIsRunning() throws IOException {
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(mockTaskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, attempt);
        doAnswer(invocation -> {
            TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.RUNNING, attempt.getId());
            eventBus.post(taskRunTransitionEvent);
            return null;
        }).when(spyBackend).startProcess(ArgumentMatchers.any(), ArgumentMatchers.any());
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());
        doAnswer(invocation ->
                invocation.callRealMethod()
        ).when(spyBackend).startProcess(ArgumentMatchers.any(), ArgumentMatchers.any());

        executor.reset();
        executor.recover();
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));
        assertThat(content, containsString("URLClassLoader"));
        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);

        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));
        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));

    }

    @Test
    //提交的taskAttempt已经下发到worker中执行
    public void submitTaskAttemptIsRunning() throws InterruptedException {
        TaskRun taskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt runningTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator8.class, runningTaskAttempt);
        executor.submit(runningTaskAttempt);
        //wait attempt Running
        awaitUntilRunning(runningTaskAttempt.getId());
        TaskAttempt createdTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        taskRunDao.createAttempt(createdTaskAttempt);
        TaskAttempt queuedTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.QUEUED);
        taskRunDao.createAttempt(queuedTaskAttempt);
        boolean submitCreated = executor.submit(createdTaskAttempt);
        boolean submitQueued = executor.submit(queuedTaskAttempt);
        assertThat(submitCreated, is(false));
        assertThat(submitQueued, is(false));

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers() - 1));

        //clean up running process
        executor.cancel(runningTaskAttempt.getId());
        awaitUntilProcessDown("default", 0);
        // events
        assertStatusHistory(runningTaskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);
    }

    @Test
    //提交的taskAttempt已经下发到worker中执行
    public void submitTaskAttemptHasFinish() {
        TaskRun taskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt finishTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, finishTaskAttempt);
        executor.submit(finishTaskAttempt);
        awaitUntilAttemptDone(finishTaskAttempt.getId());
        TaskAttempt newTaskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskRunDao.createAttempt(newTaskAttempt);

        awaitUntilProcessDown("default", 0);
        boolean result = executor.submit(newTaskAttempt);
        assertThat(result, is(true));
        // events
        assertStatusHistory(finishTaskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }


    @Test
    public void testSubmit_ok() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator1.class);

        // process
        executor.submit(attempt);

        logger.info("attemptId = {}", attempt.getId());

        // verify
        awaitUntilAttemptDone(attempt.getId());
        logger.info("task done");
        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));
        assertThat(taskRunDao.getTermAtOfTaskRun(taskRun.getId()), is(taskRun.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));
        assertThat(content, containsString("URLClassLoader"));
        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);
        awaitUntilProcessDown("default", 0);
        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));

        // inlets/outlets
        List<DataStore> inlets = taskRun.getInlets();
        List<DataStore> outlets = taskRun.getOutlets();
        assertThat(finishedEvent.getInlets(), sameBeanAs(inlets));
        assertThat(finishedEvent.getOutlets(), sameBeanAs(outlets));

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testSubmit_then_overwriteOperatorJar() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator1.class, "TestOperator1");

        // process
        executor.submit(attempt);
        awaitUntilAttemptDone(attempt.getId());
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));

        // overwrite operator jar
        attempt = prepareAttempt(TestOperator1_1.class, "TestOperator1");
        executor.submit(attempt);
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        // task_run and task_attempt
        attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());

        // logs
        log = resourceLoader.getResource(attemptProps.getLogPath());
        content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world2!"));

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testSubmit_ok_concurrent_running() throws IOException {
        // prepare
        TaskAttempt attempt1 = prepareAttempt(TestOperator1.class);
        TaskAttempt attempt2 = prepareAttempt(TestOperator2.class);

        // process
        executor.submit(attempt1);
        executor.submit(attempt2);

        // verify
        awaitUntilAttemptDone(attempt1.getId());
        awaitUntilAttemptDone(attempt2.getId());
        awaitUntilProcessDown("default", 0);

        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt1.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt2.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        // events
        assertStatusHistory(attempt1.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);

        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt1.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt1.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));

        assertStatusHistory(attempt2.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.FAILED);

        finishedEvent = getFinishedEvent(attempt2.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt2.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.FAILED));
        assertThat(finishedEvent.getInlets(), hasSize(0));
        assertThat(finishedEvent.getOutlets(), hasSize(0));


        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testSubmit_fail_running_failure() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator2.class);

        // process
        executor.submit(attempt);

        // verify
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));
        assertThat(taskRunDao.getTermAtOfTaskRun(taskRun.getId()), is(taskRun.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Execution Failed"));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.FAILED);

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testSubmit_fail_unexpected_exception() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator3.class);

        // process
        executor.submit(attempt);

        // verify
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Unexpected exception occurred"));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.FAILED);

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testSubmit_fail_operator_not_found() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator1.class, "TestOperator1", "TestOperator999");

        // process
        executor.submit(attempt);

        // verify
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Failed to load jar"));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.FAILED);

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testStop_attempt_aborted() throws Exception {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator4.class);

        // process
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers() - 1));
        awaitUntilOperatorRunning(attempt.getId());
        executor.cancel(attempt.getId());

        // wait until aborted
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        // verify
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.ABORTED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));


        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("TestOperator4 is aborting"));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testStop_attempt_force_aborted() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator5.class);

        // process
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());

        awaitUntilOperatorRunning(attempt.getId());
        executor.cancel(attempt.getId());

        // wait until aborted
        awaitUntilAttemptAbort(attempt.getId());
        awaitUntilProcessDown("default", 0);


        // verify
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.ABORTED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testAbort_attempt_cost_time() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator7.class);

        // process
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers() - 1));
        awaitUntilOperatorRunning(attempt.getId());
        executor.cancel(attempt.getId());

        // wait until aborted
        awaitUntilAttemptDone(attempt.getId());

        awaitUntilProcessDown("default", 0);

        // verify
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.ABORTED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("TestOperator7 is aborting"));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);
        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Test
    public void testStop_attempt_abort_throws_exception() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator6.class);

        // process
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());

        awaitUntilOperatorRunning(attempt.getId());
        executor.cancel(attempt.getId());

        // wait until aborted
        awaitUntilAttemptDone(attempt.getId());
        awaitUntilProcessDown("default", 0);

        // verify
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.ABORTED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(attempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Unexpected exception occurred during aborting operator."));

        // events
        assertStatusHistory(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);

        assertThat(localQueueManager.getCapacity("default"), is(localQueueManager.getResourceQueue("default").getWorkerNumbers()));
    }

    @Disabled
    public void abortTaskAttemptInQueue() {
        //prepare
        TaskAttempt taskAttempt = prepareAttempt(TestOperator1.class);

        doAnswer(invocation -> {
            //do noting to ensure worker not start
            return null;
        }).when(spyBackend).startProcess(ArgumentMatchers.any(), ArgumentMatchers.any());

        executor.submit(taskAttempt);

        //verify
        TaskAttempt saved = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(saved.getStatus(), is(TaskRunStatus.QUEUED));
        executor.cancel(taskAttempt.getId());
        awaitUntilAttemptAbort(taskAttempt.getId());
        // events
        assertStatusHistory(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.ABORTED);

        awaitUntilProcessDown("default", 0);

        assertThat(localQueueManager.getCapacity("default"), is(2));


    }

    @Test
    public void abortTaskAttemptCreated() {
        //prepare
        TaskAttempt taskAttempt = prepareAttempt(TestOperator1.class);
        //verify
        TaskAttempt saved = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(saved.getStatus(), is(TaskRunStatus.CREATED));
        executor.cancel(taskAttempt.getId());
        awaitUntilAttemptAbort(taskAttempt.getId());
        // events
        assertStatusHistory(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.ABORTED);

    }

    @Test
    public void resourceIsolationTest() {
        doAnswer(invocation -> {
            String queueName = invocation.getArgument(0, String.class);
            List<ProcessSnapShot> result = new ArrayList<>();
            if (queueName.equals("default")) {
                WorkerInstance workerInstance1 = new WorkerInstance(111, "222", "local", WorkerInstanceKind.LOCAL_PROCESS);
                ProcessSnapShot processSnapShot1 = new ProcessSnapShot(workerInstance1, TaskRunStatus.RUNNING);
                result.add(processSnapShot1);
                WorkerInstance workerInstance2 = new WorkerInstance(222, "333", "local", WorkerInstanceKind.LOCAL_PROCESS);
                ProcessSnapShot processSnapShot2 = new ProcessSnapShot(workerInstance2, TaskRunStatus.RUNNING);
                result.add(processSnapShot2);
                return result;
            } else {
                return invocation.callRealMethod();
            }
        }).when(spyBackend).fetchRunningProcess("default");
        //prepare TaskAttempt
        TaskAttempt defaultAttempt = prepareAttempt(TestOperator6.class);
        //submit default queue attempt
        executor.submit(defaultAttempt);
        Task task = MockTaskFactory.createTask().cloneBuilder().withQueueName("test").build();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        TaskAttempt userAttempt = prepareAttempt(TestOperator6.class, taskAttempt);

        // submit test attempt
        executor.submit(userAttempt);
        awaitUntilAttemptDone(userAttempt.getId());
        awaitUntilProcessDown("test", 0);

        // verify default attempt
        TaskAttemptProps defaultAttemptProps = taskRunDao.fetchLatestTaskAttempt(defaultAttempt.getTaskRun().getId());
        assertThat(defaultAttemptProps.getAttempt(), is(1));
        assertThat(defaultAttemptProps.getStatus(), is(TaskRunStatus.QUEUED));

        // verify events
        assertStatusHistory(defaultAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED);

        // verify user attempt
        TaskAttemptProps userAttemptProps = taskRunDao.fetchLatestTaskAttempt(userAttempt.getTaskRun().getId());
        assertThat(userAttemptProps.getAttempt(), is(1));
        assertThat(userAttemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(userAttemptProps.getLogPath(), is(notNullValue()));
        assertThat(userAttemptProps.getStartAt(), is(notNullValue()));
        assertThat(userAttemptProps.getEndAt(), is(notNullValue()));

        // events
        assertStatusHistory(userAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);

        //verity resource
        assertThat(localQueueManager.getCapacity("default"), is(0));
        assertThat(localQueueManager.getResourceQueue("default").getWorkerNumbers(), is(2));
        assertThat(localQueueManager.getCapacity("test"), is(2));
        assertThat(localQueueManager.getResourceQueue("test").getWorkerNumbers(), is(2));

    }

    @Test
    public void runTaskWithPriority() {
        Task task1 = MockTaskFactory.createTask().cloneBuilder().withQueueName("test").
                withPriority(16).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = prepareAttempt(TestOperator1.class, MockTaskAttemptFactory.createTaskAttempt(taskRun1));
        Task task2 = MockTaskFactory.createTask().cloneBuilder().withQueueName("test").
                withPriority(24).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = prepareAttempt(TestOperator1.class, MockTaskAttemptFactory.createTaskAttempt(taskRun2));
        Task task3 = MockTaskFactory.createTask().cloneBuilder().withQueueName("test").
                withPriority(8).build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        TaskAttempt taskAttempt3 = prepareAttempt(TestOperator1.class, MockTaskAttemptFactory.createTaskAttempt(taskRun3));
        executor.submit(taskAttempt1);
        executor.submit(taskAttempt2);
        executor.submit(taskAttempt3);

        awaitUntilAttemptDone(taskAttempt3.getId());
        awaitUntilProcessDown("test", 0);

        //verify
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(1));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps1.getLogPath(), is(notNullValue()));
        assertThat(attemptProps1.getStartAt(), is(notNullValue()));
        assertThat(attemptProps1.getEndAt(), is(notNullValue()));
        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps2.getLogPath(), is(notNullValue()));
        assertThat(attemptProps2.getStartAt(), is(notNullValue()));
        assertThat(attemptProps2.getEndAt(), is(notNullValue()));
        TaskAttemptProps attemptProps3 = taskRunDao.fetchLatestTaskAttempt(taskRun3.getId());
        assertThat(attemptProps3.getAttempt(), is(1));
        assertThat(attemptProps3.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps3.getLogPath(), is(notNullValue()));
        assertThat(attemptProps3.getStartAt(), is(notNullValue()));
        assertThat(attemptProps3.getEndAt(), is(notNullValue()));
        assertThat(attemptProps3.getStartAt(), greaterThan(attemptProps2.getStartAt()));
        assertThat(attemptProps1.getStartAt(), lessThan(attemptProps3.getStartAt()));

    }

    @Test
    public void taskRunTestCaseFailed_should_be_validate_failed() throws IOException {
        //mock data quality
        TaskAttemptCheckEventListener listener = new TaskAttemptCheckEventListener();
        listener.setValidateResult(false);
        eventBus.register(listener);

        //prepare
        Task task = MockTaskFactory.createTask().cloneBuilder()
                .withCheckType(CheckType.WAIT_EVENT)
                .build();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        prepareAttempt(TestOperator1.class, taskAttempt);

        executor.submit(taskAttempt);

        // verify
        awaitUntilAttemptDone(taskAttempt.getId());
        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.CHECK_FAILED));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun finishedTaskRun = taskRunDao.fetchLatestTaskRun(task.getId());
        assertThat(finishedTaskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(finishedTaskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(finishedTaskRun.getEndAt(), is(attemptProps.getEndAt()));
        assertThat(taskRunDao.getTermAtOfTaskRun(finishedTaskRun.getId()), is(finishedTaskRun.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));
        assertThat(content, containsString("URLClassLoader"));
        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusHistory(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.CHECK_FAILED);
        awaitUntilProcessDown("default", 0);
        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(taskAttempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(taskAttempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.CHECK_FAILED));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));

    }

    @Test
    public void taskRunTestCasePass_should_be_success() throws IOException {
        //mock data quality
        TaskAttemptCheckEventListener listener = new TaskAttemptCheckEventListener();
        listener.setValidateResult(true);
        eventBus.register(listener);

        //prepare
        Task task = MockTaskFactory.createTask().cloneBuilder()
                .withCheckType(CheckType.WAIT_EVENT)
                .build();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        prepareAttempt(TestOperator1.class, taskAttempt);

        executor.submit(taskAttempt);

        // verify
        awaitUntilAttemptDone(taskAttempt.getId());
        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun finishedTaskRun = taskRunDao.fetchLatestTaskRun(task.getId());
        assertThat(finishedTaskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(finishedTaskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(finishedTaskRun.getEndAt(), is(attemptProps.getEndAt()));
        assertThat(taskRunDao.getTermAtOfTaskRun(finishedTaskRun.getId()), is(finishedTaskRun.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));
        assertThat(content, containsString("URLClassLoader"));
        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusHistory(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);
        awaitUntilProcessDown("default", 0);
        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(taskAttempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(taskAttempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));

    }

    @Test
    public void runWorkerWithTarget() throws IOException {
        //prepare
        ExecuteTarget testTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(testTarget);
        ExecuteTarget prodTarget = ExecuteTarget.newBuilder()
                .withName("prod")
                .build();
        executeTargetService.createExecuteTarget(prodTarget);

        ExecuteTarget expectTarget = executeTargetService.fetchExecuteTarget("test");

        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task)
                .cloneBuilder()
                .withExecuteTarget(expectTarget)
                .build();
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        prepareAttempt(TestTargetOperator.class, taskAttempt);

        executor.submit(taskAttempt);

        // verify
        awaitUntilAttemptDone(taskAttempt.getId());
        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getLogPath(), is(notNullValue()));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun finishedTaskRun = taskRunDao.fetchLatestTaskRun(task.getId());
        assertThat(finishedTaskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(finishedTaskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(finishedTaskRun.getEndAt(), is(attemptProps.getEndAt()));
        assertThat(taskRunDao.getTermAtOfTaskRun(finishedTaskRun.getId()), is(finishedTaskRun.getEndAt()));

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("running target is " + expectTarget.getName()));

        // events
        assertStatusHistory(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.CHECK,
                TaskRunStatus.SUCCESS);
        awaitUntilProcessDown("default", 0);
        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(taskAttempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(taskAttempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
    }


    @Test
    public void fetchTaskRunInQueued_queued_at_should_not_null() {
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRun(taskRun);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        executor.submit(taskAttempt);
        doAnswer(invocation -> {
            String queueName = invocation.getArgument(0, String.class);
            List<ProcessSnapShot> result = new ArrayList<>();
            if (queueName.equals("default")) {
                WorkerInstance workerInstance1 = new WorkerInstance(111, "222", "local", WorkerInstanceKind.LOCAL_PROCESS);
                ProcessSnapShot processSnapShot1 = new ProcessSnapShot(workerInstance1, TaskRunStatus.RUNNING);
                result.add(processSnapShot1);
                WorkerInstance workerInstance2 = new WorkerInstance(222, "333", "local", WorkerInstanceKind.LOCAL_PROCESS);
                ProcessSnapShot processSnapShot2 = new ProcessSnapShot(workerInstance2, TaskRunStatus.RUNNING);
                result.add(processSnapShot2);
                return result;
            } else {
                return invocation.callRealMethod();
            }
        }).when(spyBackend).fetchRunningProcess("default");

        TaskRun queuedTaskRun = taskRunDao.fetchTaskRunById(taskRun.getId()).get();

        assertThat(queuedTaskRun.getQueuedAt(), notNullValue());
        assertThat(queuedTaskRun.getStatus(), is(TaskRunStatus.QUEUED));

    }


    private TaskAttempt prepareAttempt(Class<? extends KunOperator> operatorClass) {
        return prepareAttempt(operatorClass, operatorClass.getSimpleName());
    }

    private TaskAttempt prepareAttempt(Class<? extends KunOperator> operatorClass, String operatorClassName) {
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttempt();

        long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName(operatorClassName)
                .withPackagePath(compileJar(operatorClass, operatorClassName))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskDao.create(attempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(attempt.getTaskRun());
        taskRunDao.createAttempt(attempt);

        return attempt;
    }

    private TaskAttempt prepareAttempt(Class<? extends KunOperator> operatorClass, TaskAttempt attempt) {
        long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName(operatorClass.getSimpleName())
                .withPackagePath(compileJar(operatorClass, operatorClass.getSimpleName()))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskDao.create(attempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(attempt.getTaskRun());
        taskRunDao.createAttempt(attempt);
        return attempt;
    }

    private TaskAttempt prepareAttempt(Class<? extends KunOperator> operatorClass, String operatorClassName, String fakeClassName) {
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttempt();

        long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        com.miotech.kun.workflow.core.model.operator.Operator
                op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName(fakeClassName)
                .withPackagePath(compileJar(operatorClass, operatorClassName))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskDao.create(attempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(attempt.getTaskRun());
        taskRunDao.createAttempt(attempt);

        return attempt;
    }


    private String compileJar(Class<? extends KunOperator> operatorClass, String operatorClassName) {
        return OperatorCompiler.compileJar(operatorClass, operatorClassName);
    }

    private void assertStatusHistory(Long attemptId, TaskRunStatus... asserts) {
        checkArgument(asserts.length > 1);

        List<Event> events = eventCollector.getEvents();

        List<Event> eventsOfAttempt = events.stream()
                .filter(e -> e instanceof TaskAttemptStatusChangeEvent &&
                        ((TaskAttemptStatusChangeEvent) e).getAttemptId() == attemptId)
                .collect(Collectors.toList());

        logger.info(eventsOfAttempt.toString());
        for (int i = 0; i < asserts.length - 1; i++) {
            TaskAttemptStatusChangeEvent event = (TaskAttemptStatusChangeEvent) eventsOfAttempt.get(i);
            assertThat(event.getAttemptId(), is(attemptId));
            assertThat(event.getFromStatus(), is(asserts[i]));
            assertThat(event.getToStatus(), is(asserts[i + 1]));
        }
    }

    private TaskAttemptFinishedEvent getFinishedEvent(Long attemptId) {
        List<Event> events = eventCollector.getEvents();
        events = events.stream()
                .filter(e -> e instanceof TaskAttemptFinishedEvent &&
                        ((TaskAttemptFinishedEvent) e).getAttemptId().equals(attemptId))
                .collect(Collectors.toList());
        return (TaskAttemptFinishedEvent) Iterables.getOnlyElement(events);
    }

    private void awaitUntilRunning(Long attemptId) {
        await().atMost(120, TimeUnit.SECONDS)
                .until(() -> {
                    TaskAttempt attempt = taskRunDao.fetchAttemptById(attemptId).get();
                    return attempt.getStatus().equals(TaskRunStatus.RUNNING);
                });
    }


    private void awaitUntilOperatorRunning(Long attemptId) {
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(attemptId).get();
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            try {
                Resource log = resourceLoader.getResource(taskAttempt.getLogPath());
                String content = ResourceUtils.content(log.getInputStream());
                return content.contains("Operator start running");
            } catch (Exception e) {
                return false;
            }

        });
    }

    private void awaitUntilProcessDown(String queueName, Integer runningProcess) {
        await().atMost(120, TimeUnit.SECONDS).until(() ->
                localQueueManager.getCapacity(queueName) == localQueueManager.getResourceQueue(queueName).getWorkerNumbers() - runningProcess
        );
    }

    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
        });
    }

    private void awaitUntilAttemptAbort(long attemptId) {
        await().atMost(20, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isAborted());
        });
    }


    private TestWorker2 getTestWorker() {
        return new TestWorker2();
    }

    class TaskAttemptCheckEventListener {

        private boolean validateResult;

        public void setValidateResult(boolean validateResult) {
            this.validateResult = validateResult;
        }

        @Subscribe
        public void onReceive(Event event) {
            if (event instanceof TaskAttemptCheckEvent) {
                Long taskRunId = ((TaskAttemptCheckEvent) event).getTaskRunId();
                CheckResultEvent checkResultEvent = new CheckResultEvent(taskRunId, validateResult);
                mockEventSubscriber.receiveEvent(checkResultEvent);
            }
        }
    }
}
