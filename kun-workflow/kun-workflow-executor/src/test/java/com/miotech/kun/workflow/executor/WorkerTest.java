package com.miotech.kun.workflow.executor;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import com.miotech.kun.commons.rpc.RpcModule;
import com.miotech.kun.commons.rpc.RpcPublisher;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import com.miotech.kun.workflow.executor.mock.TestOperator1;
import com.miotech.kun.workflow.executor.mock.TestReconnectWorker;
import com.miotech.kun.workflow.executor.mock.TestTimeoutWorker;
import com.miotech.kun.workflow.executor.mock.TestWorker1;
import com.miotech.kun.workflow.executor.rpc.LocalExecutorFacadeImpl;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.worker.Worker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;

public class WorkerTest extends DatabaseTestBase {


    private Executor executor;

    private LocalExecutor localExecutor;

    @Inject
    private TaskRunService taskRunService;

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

    private WorkerFactory workerFactory;

    @Inject
    private WorkflowExecutorFacade localExecutorFacade;

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

    private TestLogger testLogger = TestLoggerFactory.getTestLogger(LocalExecutor.class);

    private EventCollector eventCollector;

    @Override
    protected void configuration() {
        Props props = new Props();
        String redisIp = redis.getHost();
        logger.info("redisIp:" + redisIp);
        props.put("rpc.registry", "redis://" + redisIp + ":" + redis.getMappedPort(6379));
        props.put("rpc.port", 9001);
        addModules(new RpcModule(props));
        WorkerFactory testFactory = mock(WorkerFactory.class);
        super.configuration();
        MetadataServiceFacade mockMetadataFacade = Mockito.mock(MetadataServiceFacade.class);
        WorkflowWorkerFacade mockWorkflowWorkerFacade = Mockito.mock(WorkflowWorkerFacade.class);
        bind(MetadataServiceFacade.class, mockMetadataFacade);
        bind(WorkflowWorkerFacade.class, mockWorkflowWorkerFacade);
        bind(WorkerFactory.class, testFactory);
        bind(EventBus.class, new EventBus());
        bind(WorkflowExecutorFacade.class, LocalExecutorFacadeImpl.class);
        bind(Props.class, props);
        bind(Executor.class, LocalExecutor.class);
    }

    @Before
    public void setUp() {
        executor = injector.getInstance(Executor.class);
        localExecutor = injector.getInstance(LocalExecutor.class);
        workerFactory = injector.getInstance(WorkerFactory.class);
        rpcPublisher.exportService(WorkflowExecutorFacade.class, "1.0", localExecutorFacade);
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
    }

    @Test
    //worker发送心跳超时
    public void workerHeartbeatTimeout() {
        TaskAttempt taskAttempt = prepareAttempt(TestOperator1.class);
        Worker worker = getTimeoutWorker();
        doReturn(worker).when(workerFactory).createWorker();
        doReturn(worker).when(workerFactory).getWorker(Mockito.any());
        executor.submit(taskAttempt);
        awaitUntilAttemptDone(taskAttempt.getId());
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(attempt.getStatus(), is(TaskRunStatus.TIMEOUT));
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.TIMEOUT);
        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.TIMEOUT));
        assertThat(finishedEvent.getInlets(), hasSize(0));
        assertThat(finishedEvent.getOutlets(), hasSize(0));
    }


    @Test
    //超时后worker发送心跳重新连接executor
    public void workerReconnect() {
        TaskAttempt taskAttempt = prepareAttempt(TestOperator1.class);
        Worker worker = getReconnectWorker();
        doReturn(worker).when(workerFactory).createWorker();
        doReturn(worker).when(workerFactory).getWorker(Mockito.any());
        executor.submit(taskAttempt);
        awaitUntilAttemptFailed(taskAttempt.getId());
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(attempt.getStatus(), is(TaskRunStatus.FAILED));
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.INITIALIZING,
                TaskRunStatus.TIMEOUT,
                TaskRunStatus.FAILED);
        List<TaskAttemptFinishedEvent> finishedEvents = getFinishedEvents(attempt.getId());
        assertThat(finishedEvents, hasSize(2));
        assertThat(finishedEvents.get(0).getFinalStatus(), is(TaskRunStatus.TIMEOUT));
        assertThat(finishedEvents.get(1).getFinalStatus(), is(TaskRunStatus.FAILED));
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


    private String compileJar(Class<? extends KunOperator> operatorClass, String operatorClassName) {
        return OperatorCompiler.compileJar(operatorClass, operatorClassName);
    }

    private TestReconnectWorker getReconnectWorker() {
        return new TestReconnectWorker(localExecutorFacade);
    }

    private TestTimeoutWorker getTimeoutWorker() {
        return new TestTimeoutWorker(localExecutorFacade);
    }

    private TestWorker1 getRunningWorker() {
        return new TestWorker1(localExecutorFacade);
    }

    private void awaitUntilAttemptFailed(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().equals(TaskRunStatus.FAILED));
        });
    }

    private ExecCommand buildExecCommand(TaskAttempt attempt) {
        long attemptId = attempt.getId();
        String logPath = taskRunService.logPathOfTaskAttempt(attemptId);
        logger.debug("Update logPath to TaskAttempt. attemptId={}, path={}", attemptId, logPath);
        taskRunDao.updateTaskAttemptLogPath(attemptId, logPath);
        // Operator信息
        Long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        Operator operatorDetail = operatorDao.fetchById(operatorId)
                .orElseThrow(EntityNotFoundException::new);
        logger.debug("Fetched operator's details. operatorId={}, details={}", operatorId, operatorDetail);

        ExecCommand command = new ExecCommand();
        String redisIp = redis.getContainerIpAddress();
        command.setRegisterUrl("redis://" + redisIp + ":6379");
        command.setTaskAttemptId(attemptId);
        command.setTaskRunId(attempt.getTaskRun().getId());
        command.setKeepAlive(false);
        command.setConfig(attempt.getTaskRun().getConfig());
        command.setLogPath(logPath);
        command.setJarPath(operatorDetail.getPackagePath());
        command.setClassName(operatorDetail.getClassName());
        logger.debug("Execute task. attemptId={}, command={}", attemptId, command);
        return command;
    }

    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
        });
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

    private List<TaskAttemptFinishedEvent> getFinishedEvents(Long attemptId) {
        List<Event> events = eventCollector.getEvents();
        List<TaskAttemptFinishedEvent> finishedEvents = events.stream()
                .filter(e -> e instanceof TaskAttemptFinishedEvent &&
                        ((TaskAttemptFinishedEvent) e).getAttemptId().equals(attemptId))
                .map(e -> (TaskAttemptFinishedEvent) e).collect(Collectors.toList());
        return finishedEvents;
    }


}
