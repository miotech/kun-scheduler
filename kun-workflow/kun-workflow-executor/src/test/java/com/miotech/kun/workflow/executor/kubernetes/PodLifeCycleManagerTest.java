package com.miotech.kun.workflow.executor.kubernetes;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.CommonTestBase;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@Disabled
public class PodLifeCycleManagerTest extends CommonTestBase {

    private final Logger logger = LoggerFactory.getLogger(PodLifeCycleManager.class);

    @Inject
    private PodLifeCycleManager podLifeCycleManager;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private EventBus eventBus;

    private EventCollector eventCollector;


    @Override
    protected void configuration() {
        Props props = new Props();
        props.put("executor.env.name", "KUBERNETES");
        props.put("executor.env.version", "1.15");
        props.put("executor.env.logPath", "/server/libs/logs");
        Config config = new ConfigBuilder().withMasterUrl("http://localhost:9880").build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        bind(KubernetesClient.class, client);
        bind(Props.class, props);
        bind(WorkerMonitor.class, PodEventMonitor.class);
        bind(EventPublisher.class, new NopEventPublisher());
        bind(EventSubscriber.class, mock(EventSubscriber.class));
        bind(EventBus.class, new EventBus());

        super.configuration();
    }

    @BeforeEach
    public void init() {
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
//        prepareKubernetes();
//        mockCreate();
    }

    @Test
    public void startTaskAttemptShouldSuccess() {
        TaskAttempt taskAttempt = prepareAttempt();
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

    //    @Test(expected = IllegalStateException.class)
    public void startTaskAttemptHasRunningShouldThrowException() {
        TaskAttempt taskAttempt = prepareAttempt();
        podLifeCycleManager.start(taskAttempt);

    }


    @Test
    public void stopTaskAttempt() {
        TaskAttempt taskAttempt = prepareAttempt();
        podLifeCycleManager.start(taskAttempt);
        awaitUntilRunning(taskAttempt.getId());
        podLifeCycleManager.stop(taskAttempt.getId());
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

    @Test
    public void stopFinishedTaskAttemptShouldThrowException() {
        TaskAttempt taskAttempt = prepareAttempt();
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

        //stop pod
        assertThrows(IllegalArgumentException.class, () -> podLifeCycleManager.stop(taskAttempt.getId()));


    }


    @Test
    public void testGetSnapshot() {
        TaskAttempt taskAttempt = prepareAttempt();
        podLifeCycleManager.start(taskAttempt);
        awaitUntilRunning(taskAttempt.getId());
        WorkerSnapshot workerSnapshot = podLifeCycleManager.get(taskAttempt.getId());

        //verify
        assertThat(workerSnapshot.getStatus(), is(TaskRunStatus.RUNNING));
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
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttempt();
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

    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(60, TimeUnit.SECONDS).until(() -> {
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
