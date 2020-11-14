package com.miotech.kun.workflow.executor;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import com.miotech.kun.commons.rpc.RpcModule;
import com.miotech.kun.commons.rpc.RpcPublisher;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.ReflectUtils;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import com.miotech.kun.workflow.executor.local.LocalWorkerFactory;
import com.miotech.kun.workflow.executor.mock.*;
import com.miotech.kun.workflow.executor.rpc.LocalExecutorFacadeImpl;
import com.miotech.kun.workflow.executor.rpc.WorkerClusterConsumer;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.ResourceUtils;
import com.miotech.kun.workflow.worker.Worker;
import org.joor.Reflect;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LocalExecutorTest extends DatabaseTestBase {

    private Executor executor;

    private LocalExecutor localExecutor;

    private WorkerFactory workerFactory;

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
    private RpcPublisher rpcPublisher;

    @Inject
    private WorkflowExecutorFacade localExecutorFacade;

    private WorkerFactory spyFactory;

    private static final MetadataServiceFacade mockMetadataFacade = Mockito.mock(MetadataServiceFacade.class);

    private final Logger logger = LoggerFactory.getLogger(LocalExecutorTest.class);

    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:6.0.8");

    public static GenericContainer redis = getRedis();

    public static GenericContainer getRedis() {
        GenericContainer redis = new GenericContainer(REDIS_IMAGE)
                .withExposedPorts(6379);
        redis.start();
        return redis;
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private EventCollector eventCollector;

    @Override
    protected void configuration() {
        Props props = new Props();
        String redisIp = redis.getHost();
        logger.info("redisIp:" + redisIp);
        props.put("rpc.registry", "redis://" + redisIp + ":" + redis.getMappedPort(6379));
        props.put("rpc.port", 9001);
        addModules(new RpcModule(props));
        super.configuration();
        WorkerClusterConsumer workerClusterConsumer = new WorkerClusterConsumer();
        WorkflowWorkerFacade workerFacade = workerClusterConsumer.getService("default", WorkflowWorkerFacade.class, "1.0");
        MetadataServiceFacade mockMetadataFacade = Mockito.mock(MetadataServiceFacade.class);
        bind(MetadataServiceFacade.class, mockMetadataFacade);
        bind(WorkflowWorkerFacade.class, workerFacade);
        bind(WorkerFactory.class,LocalWorkerFactory.class);
        bind(EventBus.class, new EventBus());
        bind(EventPublisher.class, new NopEventPublisher());
        bind(WorkflowExecutorFacade.class, LocalExecutorFacadeImpl.class);
        bind(Props.class, props);
        bind(Executor.class, LocalExecutor.class);
    }

    @Before
    public void setUp() {
        executor = injector.getInstance(Executor.class);
        localExecutor = injector.getInstance(LocalExecutor.class);
        workerFactory = injector.getInstance(WorkerFactory.class);
        spyFactory = spy(workerFactory);
        try {
            ReflectUtils.setField(executor,"workerFactory",spyFactory);
        }catch (IllegalAccessException | NoSuchFieldException e){
            logger.error("set spyFactory to executor failed",e);
        }
        rpcPublisher.exportService(WorkflowExecutorFacade.class, "1.0", localExecutorFacade);
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
    }

    private static class NopEventPublisher implements EventPublisher {
        @Override
        public void publish(Event event) {
            // nop
        }
    }

    private HeartBeatMessage prepareHeartBeat(TaskAttempt taskAttempt) {
        HeartBeatMessage heartBeatMessage = new HeartBeatMessage();
        heartBeatMessage.setWorkerId(1l);
        heartBeatMessage.setPort(18888);
        heartBeatMessage.setTaskAttemptId(taskAttempt.getId());
        return heartBeatMessage;
    }

    private TaskAttemptMsg prepareTaskAttemptMsg(TaskAttempt taskAttempt, HeartBeatMessage heartBeatMessage) {
        TaskAttemptMsg taskAttemptMsg = new TaskAttemptMsg();
        taskAttemptMsg.setTaskAttemptId(taskAttempt.getId());
        taskAttemptMsg.setWorkerId(heartBeatMessage.getWorkerId());
        return taskAttemptMsg;
    }

    @Test
    //taskAttempt未下发到worker执行，executor重启
    public void executorRestartBeforeWorkerStart() throws IOException {
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(mockTaskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, attempt);
        Worker localWorker = workerFactory.createWorker();
        doReturn(null).when(spyFactory).createWorker();
        executor.submit(attempt);
        executor.reset();
        doReturn(localWorker).when(spyFactory).createWorker();
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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);
        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));

    }

    @Test
    //taskAttempt未提交到executor时重启
    public void executorRestartBeforeSubmit() throws IOException {
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(mockTaskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, attempt);
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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);

        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));

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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);

        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));

    }

    @Test
    //taskAttempt下发到worker执行，executor重启,重启前销毁worker
    public void executorRestartAndKillWorker() throws IOException,InterruptedException {
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(mockTaskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, attempt);
        Worker localWorker = workerFactory.createWorker();
        Worker testWorker = getTestWorker();
        doReturn(testWorker).when(spyFactory).createWorker();
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());

        //executor shutdown and kill worker
        testWorker.killTask(false);
        //wait worker exit
        Thread.sleep(1000);
        executor.reset();
        doReturn(localWorker).when(spyFactory).createWorker();
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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ERROR,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);

        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));

    }

    @Test
    public void submitTaskAttemptHasInQueue() {
        TaskRun taskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt queuedTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        doReturn(null).when(spyFactory).createWorker();
        prepareAttempt(TestOperator1.class, queuedTaskAttempt);
        executor.submit(queuedTaskAttempt);
        TaskAttempt newTaskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        boolean result = executor.submit(newTaskAttempt);
        assertThat(result, is(false));
        //events
        assertStatusProgress(queuedTaskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
    }

    @Test
    //提交的taskAttempt已经下发到worker中执行
    public void submitTaskAttemptIsRunning() throws InterruptedException {
        TaskRun taskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt runningTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, runningTaskAttempt);
        Worker runningWorker = getRunningWorker();
        doReturn(runningWorker).when(spyFactory).createWorker();
        localExecutor.submit(runningTaskAttempt);
        //wait heartbeat
        awaitUntilRunning(runningTaskAttempt.getId());
        TaskAttempt createdTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun,TaskRunStatus.CREATED);
        TaskAttempt queuedTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun,TaskRunStatus.QUEUED);
        boolean submitCreated = localExecutor.submit(createdTaskAttempt);
        boolean submitQueued = localExecutor.submit(queuedTaskAttempt,true);
        assertThat(submitCreated, is(false));
        assertThat(submitQueued, is(false));
        // events
        awaitUntilAttemptDone(runningTaskAttempt.getId());
        assertStatusProgress(runningTaskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));

    }

    @Test
    //提交的taskAttempt已经下发到worker中执行
    public void submitTaskAttemptHasFinish() {
        TaskRun taskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt finishTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, finishTaskAttempt);
        executor.submit(finishTaskAttempt);
        awaitUntilAttemptDone(finishTaskAttempt.getId());
        TaskAttempt newTaskAttempt = MockTaskAttemptFactory.createTaskAttempt();

        boolean result = executor.submit(newTaskAttempt);
        assertThat(result, is(false));
        // events
        assertStatusProgress(finishTaskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));

    }


    @Test
    //worker执行任务状态更新
    public void workerStatusUpdate() {
        TaskAttempt attempt = prepareAttempt(TestOperator1.class);
        HeartBeatMessage heartBeatMessage = prepareHeartBeat(attempt);
        TaskAttemptMsg msg = prepareTaskAttemptMsg(attempt, heartBeatMessage);
        msg.setTaskRunStatus(TaskRunStatus.RUNNING);
        executor.statusUpdate(msg);
        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.RUNNING));

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

        // logs
        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
        String content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world!"));
        assertThat(content, containsString("URLClassLoader"));
        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);

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
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
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

        // task_run and task_attempt
        attemptProps = taskRunDao.fetchLatestTaskAttempt(attempt.getTaskRun().getId());

        // logs
        log = resourceLoader.getResource(attemptProps.getLogPath());
        content = ResourceUtils.content(log.getInputStream());
        assertThat(content, containsString("Hello, world2!"));
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
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
        assertStatusProgress(attempt1.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);

        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt1.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt1.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(finishedEvent.getInlets(), hasSize(2));
        assertThat(finishedEvent.getOutlets(), hasSize(1));

        assertStatusProgress(attempt2.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.FAILED);

        finishedEvent = getFinishedEvent(attempt2.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt2.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.FAILED));
        assertThat(finishedEvent.getInlets(), hasSize(0));
        assertThat(finishedEvent.getOutlets(), hasSize(0));
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
    }

    @Test
    public void testSubmit_fail_running_failure() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator2.class);

        // process
        executor.submit(attempt);

        // verify
        awaitUntilAttemptDone(attempt.getId());

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
        assertThat(content, containsString("Execution Failed"));

        // events
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.FAILED);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
    }

    @Test
    public void testSubmit_fail_unexpected_exception() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator3.class);

        // process
        executor.submit(attempt);

        // verify
        awaitUntilAttemptDone(attempt.getId());

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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.FAILED);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
    }

    @Test
    public void testSubmit_fail_operator_not_found() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator1.class, "TestOperator1", "TestOperator999");

        // process
        executor.submit(attempt);

        // verify
        awaitUntilAttemptDone(attempt.getId());

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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.FAILED);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
    }

    @Test
    public void testStop_attempt_aborted() throws Exception {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator4.class);

        // process
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(31));
        executor.cancel(attempt);

        // wait until aborted
        awaitUntilAttemptDone(attempt.getId());

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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);
        workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
    }

    @Test
    public void testStop_attempt_force_aborted() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator5.class);

        // process
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());
        executor.cancel(attempt);

        // wait until aborted
        awaitUntilAttemptDone(attempt.getId());

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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
    }

    @Test
    public void testStop_attempt_abort_throws_exception() throws IOException {
        // prepare
        TaskAttempt attempt = prepareAttempt(TestOperator6.class);

        // process
        executor.submit(attempt);
        awaitUntilRunning(attempt.getId());
        executor.cancel(attempt);

        // wait until aborted
        awaitUntilAttemptDone(attempt.getId());

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
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);
        Semaphore workerToken = Reflect.on(executor).field("workerToken").get();
        assertThat(workerToken.availablePermits(),is(32));
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

    private void assertStatusProgress(Long attemptId, TaskRunStatus... asserts) {
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

    private void awaitUntilInitializing(Long attemptId) {
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> {
                    TaskAttempt attempt = taskRunDao.fetchAttemptById(attemptId).get();
                    return attempt.getStatus().equals(TaskRunStatus.INITIALIZING);
                });
    }


    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
        });
    }

    private TestWorker1 getRunningWorker() {
        return new TestWorker1(localExecutorFacade);
    }

    private TestWorker2 getTestWorker(){
        return new TestWorker2(localExecutorFacade);
    }
}
