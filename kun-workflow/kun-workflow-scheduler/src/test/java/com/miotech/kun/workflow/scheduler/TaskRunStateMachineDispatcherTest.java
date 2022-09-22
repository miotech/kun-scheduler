package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.miotech.kun.workflow.TaskRunStateMachineDispatcher;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Disabled
public class TaskRunStateMachineDispatcherTest extends SchedulerTestBase {

    @Inject
    private TaskRunStateMachineDispatcher taskRunStateMachineDispatcher;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private EventBus eventBus;

    @Override
    protected void configuration() {
        super.configuration();
        bind(LineageService.class, mock(LineageService.class));
    }

    public static Stream<Arguments> prepareData() {
        Task task = MockTaskFactory.createTask();
        //prepare attempt
        TaskAttempt createdAttempt = prepareAttempt(task, TaskRunPhase.WAITING);
        TaskAttempt blockedAttempt = prepareAttempt(task, TaskRunPhase.BLOCKED);
        TaskAttempt upstreamFailedAttempt = prepareAttempt(task, TaskRunPhase.UPSTREAM_FAILED);
        TaskAttempt queuedAttempt = prepareAttempt(task, TaskRunPhase.QUEUED);
        TaskAttempt runningAttempt = prepareAttempt(task, TaskRunPhase.RUNNING);
        TaskAttempt failedAttempt = prepareAttempt(task, TaskRunPhase.FAILED);
        TaskAttempt checkAttempt = prepareAttempt(task, TaskRunPhase.CHECKING);
        TaskAttempt successAttempt = prepareAttempt(task, TaskRunPhase.SUCCESS);

        //prepare event
        TaskRunTransitionEvent createToQueuedEvent = createEvent(createdAttempt, TaskRunTransitionEventType.SUBMIT);
        TaskRunTransitionEvent createToBlockedEvent = createEvent(createdAttempt, TaskRunTransitionEventType.HANGUP);
        TaskRunTransitionEvent createToUpstreamFailedEvent = createEvent(createdAttempt, TaskRunTransitionEventType.UPSTREAM_FAILED);
        TaskRunTransitionEvent upstreamFailedToCreate = createEvent(upstreamFailedAttempt, TaskRunTransitionEventType.RESCHEDULE);
        TaskRunTransitionEvent queueToRunning = createEvent(queuedAttempt, TaskRunTransitionEventType.READY);
        TaskRunTransitionEvent runningToFailed = createEvent(runningAttempt, TaskRunTransitionEventType.FAILED);
        TaskRunTransitionEvent runningToCheck = createEvent(runningAttempt, TaskRunTransitionEventType.CHECK);
        TaskRunTransitionEvent checkToSuccess = createEvent(checkAttempt, TaskRunTransitionEventType.CHECK_SUCCESS);
        TaskRunTransitionEvent checkToCheckFailed = createEvent(checkAttempt, TaskRunTransitionEventType.CHECK_FAILED);
        TaskRunTransitionEvent failedToCreated = createEvent(failedAttempt, TaskRunTransitionEventType.RESCHEDULE);
        TaskRunTransitionEvent createToAbort = createEvent(createdAttempt, TaskRunTransitionEventType.ABORT);
        TaskRunTransitionEvent queueToAbort = createEvent(queuedAttempt, TaskRunTransitionEventType.ABORT);
        TaskRunTransitionEvent runningToAbort = createEvent(runningAttempt, TaskRunTransitionEventType.ABORT);
        TaskRunTransitionEvent blockToAbort = createEvent(blockedAttempt, TaskRunTransitionEventType.ABORT);
        TaskRunTransitionEvent checkToAbort = createEvent(checkAttempt, TaskRunTransitionEventType.ABORT);
        TaskRunTransitionEvent successToCreated = createEvent(successAttempt, TaskRunTransitionEventType.RESCHEDULE);


        return Stream.of(
                Arguments.of(createdAttempt, createToQueuedEvent, TaskRunStatus.QUEUED),
                Arguments.of(createdAttempt, createToBlockedEvent, TaskRunStatus.BLOCKED),
                Arguments.of(createdAttempt, createToUpstreamFailedEvent, TaskRunStatus.UPSTREAM_FAILED),
                Arguments.of(upstreamFailedAttempt, upstreamFailedToCreate, TaskRunStatus.CREATED),
                Arguments.of(queuedAttempt, queueToRunning, TaskRunStatus.RUNNING),
                Arguments.of(runningAttempt, runningToCheck, TaskRunStatus.CHECK),
                Arguments.of(runningAttempt, runningToFailed, TaskRunStatus.FAILED),
                Arguments.of(checkAttempt, checkToCheckFailed, TaskRunStatus.CHECK_FAILED),
                Arguments.of(checkAttempt, checkToSuccess, TaskRunStatus.SUCCESS),
                Arguments.of(failedAttempt, failedToCreated, TaskRunStatus.CREATED),
                Arguments.of(createdAttempt, createToAbort, TaskRunStatus.ABORTED),
                Arguments.of(queuedAttempt, queueToAbort, TaskRunStatus.ABORTED),
                Arguments.of(runningAttempt, runningToAbort, TaskRunStatus.ABORTED),
                Arguments.of(blockedAttempt, blockToAbort, TaskRunStatus.ABORTED),
                Arguments.of(checkAttempt, checkToAbort, TaskRunStatus.ABORTED),
                Arguments.of(successAttempt, successToCreated, TaskRunStatus.CREATED)
        );
    }

    @BeforeEach
    public void init() {
        taskRunStateMachineDispatcher.start();
    }

//    @ParameterizedTest
//    @MethodSource("prepareData")
    public void testStateTransition(TaskAttempt taskAttempt, TaskRunTransitionEvent event, TaskRunStatus nextStatus) {
        TaskRun taskRun = taskAttempt.getTaskRun();
        Task task = taskRun.getTask();
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        taskRunDao.updateTaskAttemptPhase(taskAttempt.getId(), taskAttempt.getTaskRunPhase(), null, null, null, null);

        eventBus.post(event);

        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        TaskAttempt nextAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();

        awaitUntilAttemptExpectedStatus(nextAttempt.getId(), nextStatus);
        assertThat(nextAttempt.getStatus(), is(nextStatus));


    }

    private void awaitUntilAttemptExpectedStatus(long attemptId, TaskRunStatus expected) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().equals(expected));
        });
    }

    private static TaskAttempt prepareAttempt(Task task, Integer taskRunPhase) {
        TaskRun taskRun = MockTaskRunFactory.createTaskRunWithPhase(task, taskRunPhase);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttemptWithPhase(taskRun, taskRunPhase);
        return taskAttempt;
    }

    private static TaskRunTransitionEvent createEvent(TaskAttempt taskAttempt, TaskRunTransitionEventType eventType) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(eventType, taskAttempt.getId(), null);
        return taskRunTransitionEvent;
    }
}
