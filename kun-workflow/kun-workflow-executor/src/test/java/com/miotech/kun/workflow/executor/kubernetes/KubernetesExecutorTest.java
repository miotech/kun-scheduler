package com.miotech.kun.workflow.executor.kubernetes;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.executor.CommonTestBase;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.mock.TestOperator1;
import com.miotech.kun.workflow.executor.mock.TestOperator5;
import com.miotech.kun.workflow.executor.mock.TestOperator6;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.miotech.kun.workflow.testing.operator.OperatorCompiler.compileOperatorJar;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Ignore
public class KubernetesExecutorTest extends CommonTestBase {

    private final Logger logger = LoggerFactory.getLogger(PodLifeCycleManager.class);

    @Inject
    private KubernetesExecutor kubernetesExecutor;

    @Inject
    private PodLifeCycleManager podLifeCycleManager;

    private PodLifeCycleManager spyLifeCycleManager;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private OperatorService operatorService;

    @Inject
    private EventBus eventBus;

    private EventCollector eventCollector;

    private KubernetesClient client;

    @Rule
    public final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();


    @Override
    protected String getFlywayLocation() {
        return "workflow/";
    }


    @Override
    protected void configuration() {
        Props props = new Props();
        props.put("executor.env.name", "KUBERNETES");
        props.put("executor.env.version", "1.15");
        props.put("executor.env.logPath", "/tmp/target/logs");
        props.put("executor.env.resourceQueues", "default,test");
        props.put("executor.env.resourceQueues.default.quota.cores", 2);
        props.put("executor.env.resourceQueues.default.quota.memory", 4);
        props.put("executor.env.resourceQueues.default.quota.workerNumbers", 4);
        props.put("resource.libDirectory", "lib");
        props.put("executor.env.namespace", "default");
        props.put("executor.env.nfsName", "pv-nfs");
        props.put("executor.env.nfsClaimName", "pvc-nfs");
//        props.put("executor.env.privateHub.url","hub.miotech.com/library");
        Config config = new ConfigBuilder().withMasterUrl("http://localhost:9880")
                .build();
        client = new DefaultKubernetesClient(config);
        bind(KubernetesClient.class, client);
        bind(Props.class, props);
        bind(WorkerMonitor.class, PodEventMonitor.class);
        bind(EventPublisher.class, new NopEventPublisher());
        bind(EventBus.class, new EventBus());
        super.configuration();
    }

    @Before
    public void init() {
        environmentVariables.set("DOCKER_TLS_VERIFY", "1");
        environmentVariables.set("DOCKER_HOST", "tcp://127.0.0.1:32802");
        environmentVariables.set("DOCKER_CERT_PATH", "/Users/shiki/.minikube/certs");
        environmentVariables.set("MINIKUBE_ACTIVE_DOCKERD", "minikube");
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
    }

    @After
    public void reset() {
    }

    @Test
    public void getPodList() {
        PodList podList = client.pods().inAnyNamespace().list();
        int a = 1;
        a = a + 1;
    }

    @Test
    public void startTaskAttemptShouldSuccess() {
        Task task = MockTaskFactory.createTask(1l);
        TaskRun createTaskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(createTaskRun);
        prepareAttempt(TestOperator1.class, taskAttempt, 1l);
        podLifeCycleManager.start(taskAttempt);
        awaitUntilAttemptDone(taskAttempt.getId());

        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(taskAttempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        assertStatusProgress(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);
    }

    @Test
    @Ignore
    public void startTaskAttemptWithResourceOverLimit() {
        ResourceQueue resourceQueue = ResourceQueue.newBuilder()
                .withQueueName("test")
                .withWorkerNumbers(3)
                .withMemory(1)
                .withCores(1)
                .build();
        kubernetesExecutor.createResourceQueue(resourceQueue);
        TaskAttempt taskAttempt = prepareAttempt().cloneBuilder().withQueueName("test").build();
        podLifeCycleManager.start(taskAttempt);
        awaitUntilAttemptDone(taskAttempt.getId());

        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(taskAttempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        assertStatusProgress(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);

    }

    @Test
    //提交的taskAttempt已经下发到worker中执行
    public void submitTaskAttemptHasFinish() {
        Task task = MockTaskFactory.createTask(1l);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt finishTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator1.class, finishTaskAttempt, 1l);
        kubernetesExecutor.submit(finishTaskAttempt);
        awaitUntilAttemptDone(finishTaskAttempt.getId());
        TaskAttempt newTaskAttempt = MockTaskAttemptFactory.createTaskAttempt();

        boolean result = kubernetesExecutor.submit(newTaskAttempt);
        assertThat(result, is(false));
        // events
        assertStatusProgress(finishTaskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);
    }

    @Test
    //提交的taskAttempt已经下发到worker中执行
    public void submitTaskAttemptIsRunning() {
        Task task = MockTaskFactory.createTask(5l);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt runningTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        prepareAttempt(TestOperator5.class, runningTaskAttempt, 5l);
        awaitUntilRunning(runningTaskAttempt.getId());
        TaskAttempt createdTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.CREATED);
        TaskAttempt queuedTaskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun, TaskRunStatus.QUEUED);
        boolean submitCreated = kubernetesExecutor.submit(createdTaskAttempt);
        boolean submitQueued = kubernetesExecutor.submit(queuedTaskAttempt);
        assertThat(submitCreated, is(false));
        assertThat(submitQueued, is(false));
        // events
        assertStatusProgress(runningTaskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING);
    }


    @Test
    public void executorRestartWhenWorkerRunning() {
        Task task = MockTaskFactory.createTask(5l);
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttempt(mockTaskRun);
        prepareAttempt(TestOperator5.class, attempt, 5l);
        kubernetesExecutor.submit(attempt);
        awaitUntilRunning(attempt.getId());
        kubernetesExecutor.reset();
        kubernetesExecutor.recover();
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
//        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
//        String content = ResourceUtils.content(log.getInputStream());
//        assertThat(content, containsString("Hello, world!"));
//        assertThat(content, containsString("URLClassLoader"));
//        assertThat(content, not(containsString("AppClassLoader")));

        // events
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.SUCCESS);
//        TaskAttemptFinishedEvent finishedEvent = getFinishedEvent(attempt.getId());
//        assertThat(finishedEvent.getAttemptId(), is(attempt.getId()));
//        assertThat(finishedEvent.getFinalStatus(), is(TaskRunStatus.SUCCESS));
//        assertThat(finishedEvent.getInlets(), hasSize(2));
//        assertThat(finishedEvent.getOutlets(), hasSize(1));

    }

    @Test
    public void testStop_attempt_abort_throws_exception() {
        Task task = MockTaskFactory.createTask(6l);
        TaskRun mockTaskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttempt(mockTaskRun);
        // prepare
        prepareAttempt(TestOperator6.class, attempt, 6l);

        // process
        kubernetesExecutor.submit(attempt);
        awaitUntilRunning(attempt.getId());
        kubernetesExecutor.cancel(attempt.getId());

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
//        Resource log = resourceLoader.getResource(attemptProps.getLogPath());
//        String content = ResourceUtils.content(log.getInputStream());
//        assertThat(content, containsString("Unexpected exception occurred during aborting operator."));

        // events
        assertStatusProgress(attempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);
    }

    @Test
    @Ignore
    public void testChangePodPriority() {

    }

    @Test
    public void testAbortPod() {
        Task task = MockTaskFactory.createTask(1l);
        TaskRun createTaskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(createTaskRun);
        prepareAttempt(TestOperator1.class, taskAttempt, 1l);
        kubernetesExecutor.submit(taskAttempt);
        awaitUntilRunning(taskAttempt.getId());
        kubernetesExecutor.cancel(taskAttempt.getId());
        awaitUntilAttemptDone(taskAttempt.getId());

        // task_run and task_attempt
        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.ABORTED));
        assertThat(attemptProps.getStartAt(), is(notNullValue()));
        assertThat(attemptProps.getEndAt(), is(notNullValue()));

        TaskRun taskRun = taskRunDao.fetchLatestTaskRun(taskAttempt.getTaskRun().getTask().getId());
        assertThat(taskRun.getStatus(), is(attemptProps.getStatus()));
        assertThat(taskRun.getStartAt(), is(attemptProps.getStartAt()));
        assertThat(taskRun.getEndAt(), is(attemptProps.getEndAt()));

        assertStatusProgress(taskAttempt.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.RUNNING,
                TaskRunStatus.ABORTED);

    }

    private TaskAttemptFinishedEvent getFinishedEvent(Long attemptId) {
        List<Event> events = eventCollector.getEvents();
        events = events.stream()
                .filter(e -> e instanceof TaskAttemptFinishedEvent &&
                        ((TaskAttemptFinishedEvent) e).getAttemptId().equals(attemptId))
                .collect(Collectors.toList());
        return (TaskAttemptFinishedEvent) Iterables.getOnlyElement(events);
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


    private TaskAttempt prepareAttempt() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("NopOperator")
                .withClassName("NopOperator")
                .withPackagePath("/server/libs/NopOperator.jar")
                .build();
        operatorDao.createWithId(op, operatorId);
        taskDao.create(attempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(attempt.getTaskRun());
        taskRunDao.createAttempt(attempt);
        return attempt;
    }

    private TaskAttempt prepareAttempt(Class<? extends KunOperator> operatorClass, TaskAttempt attempt, Long operatorId) {
        String packagePath = compileOperatorJar("/tmp/lib/" + operatorId, operatorClass, operatorClass.getSimpleName() + "-operator");
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName(operatorClass.getSimpleName() + "-operator")
                .withClassName(operatorClass.getSimpleName())
                .withPackagePath(packagePath)
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


    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
        });
    }

    private void awaitUntilRunning(Long attemptId) {
        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> {
                    TaskAttempt attempt = taskRunDao.fetchAttemptById(attemptId).get();
                    return attempt.getStatus().equals(TaskRunStatus.RUNNING);
                });
    }

    private static class NopEventPublisher implements EventPublisher {
        @Override
        public void publish(Event event) {
            // nop
        }
    }

}
