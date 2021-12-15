package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.TaskRunStateMachine;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TaskRunStateMachineTest extends DatabaseTestBase {

    private TaskRunStateMachine taskRunStateMachine;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private EventBus eventBus;

    private TaskAttempt taskAttempt;

    private TaskRunTransitionEvent event;

    private TaskRunStatus nextStatus;

    public TaskRunStateMachineTest(TaskAttempt taskAttempt, TaskRunTransitionEvent event, TaskRunStatus nextStatus){
        this.taskAttempt = taskAttempt;
        this.event = event;
        this.nextStatus = nextStatus;
    }


    @Parameterized.Parameters
    @SuppressWarnings("unchecked")
    public static Collection prepareData(){
        Task task = MockTaskFactory.createTask();
        //prepare attempt
        TaskAttempt createdAttempt = prepareAttempt(task,TaskRunStatus.CREATED);
        TaskAttempt blockedAttempt = prepareAttempt(task,TaskRunStatus.BLOCKED);
        TaskAttempt upstreamFailedAttempt = prepareAttempt(task,TaskRunStatus.UPSTREAM_FAILED);
        TaskAttempt queuedAttempt = prepareAttempt(task,TaskRunStatus.QUEUED);
        TaskAttempt runningAttempt = prepareAttempt(task,TaskRunStatus.RUNNING);
        TaskAttempt failedAttempt = prepareAttempt(task,TaskRunStatus.FAILED);
        TaskAttempt checkAttempt = prepareAttempt(task,TaskRunStatus.CHECK);

        //prepare event
        TaskRunTransitionEvent createToQueuedEvent = createEvent(createdAttempt, TaskRunTransitionEventType.SUBMIT);
        TaskRunTransitionEvent createToBlockedEvent = createEvent(createdAttempt, TaskRunTransitionEventType.HANGUP);
        TaskRunTransitionEvent createToUpstreamFailedEvent = createEvent(createdAttempt, TaskRunTransitionEventType.UPSTREAM_FAILED);
        TaskRunTransitionEvent blockToCreateEvent = createEvent(blockedAttempt, TaskRunTransitionEventType.AWAKE);
        TaskRunTransitionEvent upstreamFailedToCreate = createEvent(upstreamFailedAttempt, TaskRunTransitionEventType.RESCHEDULE);
        TaskRunTransitionEvent queueToRunning = createEvent(queuedAttempt, TaskRunTransitionEventType.RUNNING);
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



        Object [][] bject = {
                {createdAttempt,createToQueuedEvent,TaskRunStatus.QUEUED},
                {createdAttempt,createToBlockedEvent,TaskRunStatus.BLOCKED},
                {createdAttempt,createToUpstreamFailedEvent,TaskRunStatus.UPSTREAM_FAILED},
                {blockedAttempt,blockToCreateEvent,TaskRunStatus.CREATED},
                {upstreamFailedAttempt,upstreamFailedToCreate,TaskRunStatus.CREATED},
                {queuedAttempt,queueToRunning,TaskRunStatus.RUNNING},
                {runningAttempt,runningToCheck,TaskRunStatus.CHECK},
                {runningAttempt,runningToFailed,TaskRunStatus.FAILED},
                {checkAttempt,checkToCheckFailed,TaskRunStatus.CHECK_FAILED},
                {checkAttempt,checkToSuccess,TaskRunStatus.SUCCESS},
                {failedAttempt,failedToCreated,TaskRunStatus.CREATED},
                {createdAttempt,createToAbort,TaskRunStatus.ABORTED},
                {queuedAttempt,queueToAbort,TaskRunStatus.ABORTED},
                {runningAttempt,runningToAbort,TaskRunStatus.ABORTED},
                {blockedAttempt,blockToAbort,TaskRunStatus.ABORTED},
                {checkAttempt,checkToAbort,TaskRunStatus.ABORTED}
        };
        return Arrays.asList(bject);
    }

    @Before
    public void init(){
        taskRunStateMachine = new TaskRunStateMachine(taskRunDao,eventBus,mock(LineageService.class));
        taskRunStateMachine.start();
    }

    @Test
    public void testStateTransition(){
        TaskRun taskRun = taskAttempt.getTaskRun();
        Task task = taskRun.getTask();
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);

        eventBus.post(event);

        TaskAttempt nextAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();

        assertThat(nextAttempt.getStatus(),is(nextStatus));



    }

    private static TaskAttempt prepareAttempt(Task task, TaskRunStatus taskRunStatus){
        TaskRun taskRun = MockTaskRunFactory.createTaskRunWithStatus(task,taskRunStatus);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttemptWithStatus(taskRun,taskRunStatus);
        return taskAttempt;
    }

    private static TaskRunTransitionEvent createEvent(TaskAttempt taskAttempt, TaskRunTransitionEventType eventType){
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(eventType,taskAttempt.getId());
        return taskRunTransitionEvent;
    }
}
