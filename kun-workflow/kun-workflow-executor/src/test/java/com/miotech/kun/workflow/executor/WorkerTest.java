package com.miotech.kun.workflow.executor;

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
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
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
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
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

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
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
    private WorkflowExecutorFacade  localExecutorFacade;

    private final Long HEARTBEAT_INTERVAL = 5 * 1000l;

    private static final MetadataServiceFacade mockMetadataFacade = Mockito.mock(MetadataServiceFacade.class);

    private final Logger logger = LoggerFactory.getLogger(LocalExecutorTest.class);

    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:6.0.8");

//    @ClassRule
    public GenericContainer redis = new GenericContainer(REDIS_IMAGE)
            .withExposedPorts(6379);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private EventCollector eventCollector;

    @Override
    protected void configuration() {
        Props props = new Props();
        redis.start();
        String redisIp = redis.getHost();

        logger.info("redisIp:" + redisIp);
        props.put("rpc.registry", "redis://" + redisIp + ":" + redis.getFirstMappedPort());
        props.put("rpc.port", 9001);
        addModules(new RpcModule(props));
        WorkerFactory testFactory = mock(WorkerFactory.class);
        super.configuration();
        MetadataServiceFacade mockMetadataFacade = Mockito.mock(MetadataServiceFacade.class);
        WorkflowWorkerFacade mockWorkflowWorkerFacade = Mockito.mock(WorkflowWorkerFacade.class);
        bind(MetadataServiceFacade.class,mockMetadataFacade);
        bind(WorkflowWorkerFacade.class,mockWorkflowWorkerFacade);
        bind(WorkerFactory.class, testFactory);
        bind(EventBus.class, new EventBus());
        bind(WorkflowExecutorFacade.class, LocalExecutorFacadeImpl.class);
        bind(Props.class,props);
        bind(Executor.class, LocalExecutor.class);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        executor = injector.getInstance(Executor.class);
        localExecutor = injector.getInstance(LocalExecutor.class);
        workerFactory = injector.getInstance(WorkerFactory.class);
        rpcPublisher.exportService(WorkflowExecutorFacade.class,"1.0", localExecutorFacade);
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
    }

    @Test
    //worker发送心跳超时
    public void workerHeartbeatTimeout(){
        TaskAttempt taskAttempt = prepareAttempt(TestOperator1.class);
        Worker worker = getTimeoutWorker();
        doReturn(worker).when(workerFactory).createWorker();
        doReturn(worker).when(workerFactory).getWorker(Mockito.any());
        executor.submit(taskAttempt);
        awaitUntilAttemptDone(taskAttempt.getId());
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(attempt.getStatus(),is(TaskRunStatus.FAILED));
    }



    @Test
    //超时后worker发送心跳重新连接executor
    public void workerReconnect(){
        TaskAttempt taskAttempt = prepareAttempt(TestOperator1.class);
        Worker worker = getReconnectWorker();
        doReturn(worker).when(workerFactory).createWorker();
        doReturn(worker).when(workerFactory).getWorker(Mockito.any());
        executor.submit(taskAttempt);
        awaitUntilAttemptAbort(taskAttempt.getId());
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(attempt.getStatus(),is(TaskRunStatus.ABORTED));
    }

    @Test
    //提交的taskAttempt已经下发到worker中执行
    public void submitTaskAttemptIsRunning() throws InterruptedException{
        TaskRun taskRun = MockTaskRunFactory.createTaskRun();
        TaskAttempt queuedTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun,TaskRunStatus.QUEUED);
        prepareAttempt(TestOperator1.class,queuedTaskAttempt);
        ExecCommand execCommand = buildExecCommand(queuedTaskAttempt);
        Worker runningWorker = getRunningWorker();
        runningWorker.start(execCommand);
        //wait heartbeat
        Thread.sleep(2000);
//        TaskAttempt newTaskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        boolean result = localExecutor.submit(queuedTaskAttempt,true);
        assertThat(result,is(false));
        awaitUntilAttemptDone(queuedTaskAttempt.getId());
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(queuedTaskAttempt.getTaskRun().getId());
        assertThat(attempt.getStatus(),is(TaskRunStatus.FAILED));
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

    private TaskAttempt prepareAttempt(Class<? extends KunOperator> operatorClass,TaskAttempt attempt){
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

    private TestReconnectWorker getReconnectWorker(){
        return new TestReconnectWorker(localExecutorFacade);
    }

    private TestTimeoutWorker getTimeoutWorker(){
        return new TestTimeoutWorker(localExecutorFacade);
    }

    private TestWorker1 getRunningWorker(){
        return new TestWorker1(localExecutorFacade);
    }

    private void awaitUntilAttemptAbort(long attemptId) {
        await().atMost(600, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isAborted());
        });
    }

    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(600, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
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

}
