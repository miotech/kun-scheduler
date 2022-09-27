package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.workflow.TaskRunSMExecutor;
import com.miotech.kun.workflow.TaskRunStateMachineBuffer;
import com.miotech.kun.workflow.TaskRunStateMachineDispatcher;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.model.common.Condition;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.BlockType;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.*;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.testing.operator.NopOperator;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.miotech.kun.workflow.core.event.TaskRunTransitionEventType.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class TaskManagerTest extends SchedulerTestBase {
    @Inject
    private TaskManager taskManager;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private EventBus eventBus;

    @Inject
    private Executor executor;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private TaskRunStateMachineDispatcher taskRunStateMachineDispatcher;

    @Inject
    private TaskRunSMExecutor taskRunSMExecutor;

    @Inject
    private TaskRunStateMachineBuffer buffer;

    private EventCollector eventCollector;


    private static final String CRON_EVERY_MINUTE = "0 * * ? * * *";

    @Override
    protected void configuration() {
        super.configuration();
        executor = mock(Executor.class);
        bind(LineageService.class, mock(LineageService.class));
    }

    @BeforeEach
    public void init() {
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
        taskRunStateMachineDispatcher.start();
    }

    @Test
    public void testSubmit_task_with_no_dependency() {
        // prepare
        TaskRun taskRun = MockTaskRunFactory.createTaskRun();
        taskDao.create(taskRun.getTask());
        taskRunDao.createTaskRun(taskRun);

        // process
        taskManager.submit(Lists.newArrayList(taskRun));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);

        List<TaskAttempt> result = getSubmittedTaskAttempts();
        assertThat(result, is(hasSize(1)));

        TaskAttempt taskAttempt = result.get(0);
        assertThat(taskAttempt.getId(), is(WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), 1)));
        assertThat(taskAttempt.getAttempt(), is(1));
        checkTaskRun(taskAttempt.getTaskRun(), taskRun);
        assertThat(taskAttempt.getLogPath(), is(nullValue()));
        assertThat(taskAttempt.getStartAt(), is(nullValue()));
        assertThat(taskAttempt.getEndAt(), is(nullValue()));

        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(attemptProps.getId(), is(taskAttempt.getId()));
        // TODO: non-passed yet
        // assertThat(attemptProps.getTaskName(), is(taskAttempt.getTaskName()));
        // assertThat(attemptProps.getTaskId(), is(taskAttempt.getTaskId()));
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getLogPath(), is(nullValue()));
        assertThat(attemptProps.getStartAt(), is(nullValue()));
        assertThat(attemptProps.getEndAt(), is(nullValue()));
    }

    @Test
    public void testSubmit_task_upstream_is_success() {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1");
        for (TaskRun taskRun : taskRuns) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        TaskRun taskRun1 = taskRuns.get(0);
        TaskRun taskRun2 = taskRuns.get(1);

        TaskAttempt attempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        taskRunDao.createAttempt(attempt1);
        taskRunDao.updateTaskAttemptPhase(attempt1.getId(), TaskRunPhase.SUCCESS, null, null, null, null);
        taskRunDao.updateConditionsWithTaskRuns(Collections.singletonList(taskRun1.getId()), TaskRunPhase.SUCCESS);

        // process
        taskManager.submit(Lists.newArrayList(taskRun2));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);

        List<TaskAttempt> result = getSubmittedTaskAttempts();
        assertThat(result, is(hasSize(1)));

        TaskAttempt taskAttempt = result.get(0);
        assertThat(taskAttempt.getId(), is(WorkflowIdGenerator.nextTaskAttemptId(taskRun2.getId(), 1)));
        assertThat(taskAttempt.getAttempt(), is(1));
        assertThat(taskAttempt.getTaskRun().getId(), is(taskRun2.getId()));
        checkTaskRun(taskAttempt.getTaskRun(), taskRun2);
        assertThat(taskAttempt.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskAttempt.getLogPath(), is(nullValue()));
        assertThat(taskAttempt.getStartAt(), is(nullValue()));
        assertThat(taskAttempt.getEndAt(), is(nullValue()));
    }

    @Test
    public void testSubmit_task_upstream_is_failed() throws InterruptedException {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1");
        for (TaskRun taskRun : taskRuns) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        TaskRun taskRun1 = taskRuns.get(0);
        TaskRun taskRun2 = taskRuns.get(1);

        TaskAttempt attempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        taskRunDao.createAttempt(attempt1);
        taskRunDao.updateTaskAttemptStatus(attempt1.getId(), TaskRunStatus.FAILED);

        // process
        taskManager.submit(Lists.newArrayList(taskRun2));

        // verify
        TimeUnit.SECONDS.sleep(2);
        assertThat(invoked(), is(false));
    }


    private void checkTaskRun(TaskRun actual, TaskRun except) {
        assertThat(actual.getId(), is(except.getId()));
        assertThat(actual.getPriority(), is(except.getPriority()));
        assertThat(actual.getQueueName(), is(except.getQueueName()));
        assertThat(actual.getScheduledTick(), is(except.getScheduledTick()));
        assertThat(actual.getScheduledType(), is(except.getScheduledType()));
    }

    @Test
    public void rerunSuccessTaskRun_shouldExecute() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withStatus(TaskRunStatus.SUCCESS).build();
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(Arrays.asList(taskRun));

        awaitUntilAttemptDone(taskRun.getId() + 1);

        taskManager.retry(taskRun);

        awaitUntilAttemptDone(taskRun.getId() + 2);

    }

    @Test
    public void rerunFailedTaskRun_shouldRemoveDownstreamFailedTaskrunIds() throws InterruptedException {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun1, taskRun2));

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);

        TaskRun result = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        MatcherAssert.assertThat(result.getFailedUpstreamTaskRunIds().size(), is(1));

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.retry(taskRunDao.fetchTaskRunById(taskRun1.getId()).get());

        awaitUntilAttemptRunning(taskRun1.getId() + 2);
        TimeUnit.SECONDS.sleep(2);

        result = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();

        MatcherAssert.assertThat(result.getFailedUpstreamTaskRunIds(), is(Collections.emptyList()));
    }

    //taskrun1 is 2's predecessor
    //when taskrun1 retry, 2 from created to blocked
    @Disabled
    public void rerunTaskRunEffectSuccessor_shouldChangedToBlocked() {
        Task task = MockTaskFactory.createTask().cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build();
        taskDao.create(task);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRunWithStatus(task, TaskRunStatus.CREATED);
        TaskRunCondition taskRunCondition = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .withResult(true).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRunWithStatus(task, TaskRunStatus.CREATED)
                .cloneBuilder().withTaskRunConditions(Collections.singletonList(taskRunCondition)).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            if (taskAttempt.getId().equals(taskRun1.getId() + 1)) {
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(failedEvent);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun1, taskRun2));

        awaitUntilAttemptDone(taskRun1.getId() + 1);
        TaskRun saved2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        assertThat(saved2.getStatus(), is(TaskRunStatus.CREATED));

        //retry taskRun1
        taskManager.retry(taskRun1);

        awaitUntilAttemptBlock(taskRun2.getId() + 1);
        TaskRun result = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();

        MatcherAssert.assertThat(result.getStatus(), is(TaskRunStatus.BLOCKED));
    }

    @Test
    public void upstreamFailedShouldUpdateDownstreamStatus() {

        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2");

        long operatorId = taskList.get(0).getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("testOperator")
                .withPackagePath(compileJar(NopOperator.class, NopOperator.class.getSimpleName()))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>2");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);


        awaitUntilAttemptDone(taskRun1.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun3.getId() + 1);

        // verify update downStream
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(1));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps1.getStartAt(), is(notNullValue()));
        assertThat(attemptProps1.getEndAt(), is(notNullValue()));

        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        assertThat(attemptProps2.getStartAt(), is(nullValue()));
        assertThat(attemptProps2.getEndAt(), is(nullValue()));
        assertThat(taskRunDao.getTermAtOfTaskRun(taskRun2.getId()), is(notNullValue()));

        TaskAttemptProps attemptProps3 = taskRunDao.fetchLatestTaskAttempt(taskRun3.getId());
        assertThat(attemptProps3.getAttempt(), is(1));
        assertThat(attemptProps3.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        assertThat(attemptProps3.getStartAt(), is(nullValue()));
        assertThat(attemptProps3.getEndAt(), is(nullValue()));
        assertThat(taskRunDao.getTermAtOfTaskRun(taskRun3.getId()), is(notNullValue()));
    }

    @Test
    //scenario: 0>>1 1>>2
    // when 0 failed, add 0 to failedUpstreamTaskRun of 1,2
    public void upstreamFail_UpdateMultiDownstreamTaskRunIdsWithSingleFailedTaskRun_Success() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2");
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>2");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun3.getId() + 1);

        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).size(), is(1));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun3.getId()).size(), is(1));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).get(0).getId(), is(taskRun1.getId()));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun3.getId()).get(0).getId(), is(taskRun1.getId()));
    }

    @Test
    //scenario: 0>>1 0>>2 1,2>>3
    //         0 failed, update 0 to failedUpstreamTaskRun to 1,2,3 and to 3 ONLY ONCE
    public void mutualUpstreamFail_UpdateDownstreamTaskRunOnlyOnce_Success() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;0>>2;1>>3;2>>3");
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;0>>2;1>>3;2>>3");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        TaskRun taskRun4 = taskRunList.get(3);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);

        awaitUntilAttemptDone(taskRun1.getId() + 1);
        //wait event finish
        Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);

        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun4.getId()).size(), is(1));

    }


    @Test
    //scenario: 0>>1 1>>2.
    // 0 failed, 1&2 -> upstream failed. 0 is added to failedUpstreamTaskRunIds of 1&2
    // retry 0, 1&2 -> created. failedUpstreamTaskRunIds of 1&2 is removed.
    public void retryFailedUpstream_UpdateMultiDownstreamTaskRunsWhenTaskRunRetry_Success() throws InterruptedException {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2");
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>2");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun3.getId() + 1);

        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).get(0).getId(), is(taskRun1.getId()));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun3.getId()).get(0).getId(), is(taskRun1.getId()));

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.retry(taskRunDao.fetchTaskRunById(taskRun1.getId()).get());

        TimeUnit.SECONDS.sleep(2);

        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).isEmpty(), is(true));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun3.getId()).isEmpty(), is(true));

    }

    @Test
    // scenario: 0>>1 1>>3 2>>3.
    //         0&2 failed
    //         status of 1&3 -> upstream failed
    //         0 is added to failedUpstreamTaskRunIds of 1&3
    //         2 is added to failedUpstreamTaskRunIds of 3
    public void upstreamFail_UpdateMultiDownstreamTaskRunIdsWithMultiFailedTaskRun_Success() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;1>>3;2>>3");

        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>3;2>>3");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        TaskRun taskRun4 = taskRunList.get(3);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId()) || taskAttempt.getTaskRun().getId().equals(taskRun3.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);

        awaitUntilAttemptDone(taskRun3.getId() + 1);
        //wait event finish
        Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);

        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).size(), is(1));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun4.getId()).size(), is(2));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).get(0).getId(), is(taskRun1.getId()));
        assertThat(new HashSet<>(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun4.getId()).stream()
                        .map(TaskRun::getId).collect(Collectors.toList())),
                is(new HashSet<>(Arrays.asList(taskRun1.getId(), taskRun3.getId()))));
    }


    @Test
    // scenario: 0>>1 1>>3 2>>3.
    // Step 1: 0&2 failed
    //         status of 1&3 -> upstream failed
    //         0 is added to failedUpstreamTaskRunIds of 1&3
    //         2 is added to failedUpstreamTaskRunIds of 3
    // Step 2: rerun 0
    //         status of 1 -> created.
    //         failedUpstreamTaskRunIds 0 of 1&3 is removed.
    //         failedUpstreamTaskRunIds 2 of 3 is remained.
    public void retryFailedUpstream_UpdateMultiDownstreamTaskRunIdsWithMultiFailedTaskRun_Success() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;1>>3;2>>3");
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>3;2>>3");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        TaskRun taskRun4 = taskRunList.get(3);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);


        //make taskRun 1 & 3 fail
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId()) || taskAttempt.getTaskRun().getId().equals(taskRun3.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);

        awaitUntilAttemptDone(taskRun3.getId() + 1);
        //wait event finish
        Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);

        //verify failed upstream taskRuns of taskRun 2 & 4
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).size(), is(1));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun4.getId()).size(), is(2));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).get(0).getId(), is(taskRun1.getId()));
        assertThat(new HashSet<>(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun4.getId()).stream()
                        .map(TaskRun::getId).collect(Collectors.toList())),
                is(new HashSet<>(Arrays.asList(taskRun1.getId(), taskRun3.getId()))));

        //retry taskRun 1
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.retry(taskRunDao.fetchTaskRunById(taskRun1.getId()).get());

        awaitUntilAttemptDone(taskRun1.getId() + 2);
        //wait event finish
        Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);

        //verify failed upstream taskRuns of taskRun 2 & 4
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).size(), is(0));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun4.getId()).size(), is(1));
        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun4.getId()).get(0).getId(), is(taskRun3.getId()));

    }

    //scenario: task 0; taskRun 1 is 0's predecessor
    // 0 is running, 1 is blocked
    // when 0 is failed,  1 changed to created
    @Test
    public void predecessorFinish_successorStatusToCREATED_success() throws InterruptedException {
        Task task = MockTaskFactory.createTask().cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build();
        taskDao.create(task);
        TaskRun taskRun0 = MockTaskRunFactory.createTaskRunWithStatus(task, TaskRunStatus.CREATED);
        TaskRunCondition taskRunCondition = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun0.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .withResult(false).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRunWithStatus(task, TaskRunStatus.CREATED)
                .cloneBuilder().withTaskRunConditions(Collections.singletonList(taskRunCondition)).build();
        taskRunDao.createTaskRun(taskRun0);
        taskRunDao.createTaskRun(taskRun1);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        //process
        taskManager.submit(Arrays.asList(taskRun0, taskRun1));
        awaitUntilAttemptRunning(taskRun0.getId() + 1);


        TaskRun saved1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        assertThat(saved1.getStatus(), is(TaskRunStatus.BLOCKED));

        //taskRun0 failed
        TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskRun0.getId() + 1, null);
        eventBus.post(failedEvent);


        awaitUntilAttemptRunning(taskRun1.getId() + 1);

        //check
        saved1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        assertThat(saved1.getId(), is(taskRun1.getId()));
        assertThat(saved1.getStatus(), is(TaskRunStatus.RUNNING));
    }

    //scenario: task 0>>1; taskRun 2 is 0's predecessor
    // 0 is success, 1 is running, so 2 is blocked
    // when 1 is failed,  2 changed to created
    @Test
    public void predecessorDownstreamFinish_successorStatusToCREATED_success() throws InterruptedException {
        //prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1");
        Task task0 = tasks.get(0).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR_DOWNSTREAM))
                .build();
        Task task1 = tasks.get(1);
        taskDao.create(task0);
        taskDao.create(task1);
        TaskRun taskRun0 = taskRuns.get(0);
        TaskRun taskRun1 = taskRuns.get(1);
        //prepare taskRun2
        List<TaskRunCondition> taskRunConditions = new ArrayList<>();
        TaskRunCondition taskRunCondition0 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun0.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .withResult(false)
                .build();
        taskRunConditions.add(taskRunCondition0);
        TaskRunCondition taskRunCondition1 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .withResult(false)
                .build();
        taskRunConditions.add(taskRunCondition1);

        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task0).cloneBuilder()
                .withTaskRunConditions(taskRunConditions).build();
        taskRunDao.createTaskRun(taskRun0);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            if (taskAttempt.getTaskRun().getId().equals(taskRun0.getId())) {
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        //process
        taskManager.submit(Arrays.asList(taskRun0, taskRun1, taskRun2));
        awaitUntilAttemptRunning(taskRun1.getId() + 1);


        TaskRun saved2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        assertThat(saved2.getStatus(), is(TaskRunStatus.BLOCKED));

        //taskRun1 failed
        TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskRun1.getId() + 1, null);
        eventBus.post(failedEvent);


        awaitUntilAttemptRunning(taskRun2.getId() + 1);

        //check
        saved2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        assertThat(saved2.getId(), is(taskRun2.getId()));
        assertThat(saved2.getStatus(), is(TaskRunStatus.RUNNING));


    }

    //scenario: task 0>>1>>2, taskrun 0>>1>>2, taskrun 2 is predecessor of 3
    // task2 is SCHEDULED with blockType: wait_predecessor
    // currently taskrun 0 running, taskrun 1 2 created, taskrun 3 blocked
    // when taskrun 0 failed, 1&2 upstream_failed, 3 should be CREATED
    @Test
    public void farUpstreamFailed_terminateDownstream_blockedShouldCancel() throws InterruptedException {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>2");
        Task task0 = taskList.get(0);
        Task task1 = taskList.get(1);
        Task task2 = taskList.get(2).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build();
        taskDao.create(task0);
        taskDao.create(task1);
        taskDao.create(task2);
        TaskRun taskRun0 = taskRunList.get(0);
        TaskRun taskRun1 = taskRunList.get(1);
        TaskRun taskRun2 = taskRunList.get(2);
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task2).cloneBuilder()
                .withTaskRunConditions(Collections.singletonList(TaskRunCondition.newBuilder()
                        .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun2.getId().toString())))
                        .withResult(false)
                        .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                        .build()))
                .build();
        taskRunDao.createTaskRun(taskRun0);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);


        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            if (taskAttempt.getTaskRun().getId().equals(taskRun0.getId())) {
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());


        //process
        taskManager.submit(Arrays.asList(taskRun0, taskRun1, taskRun2, taskRun3));

        awaitUntilAttemptRunning(taskRun1.getId() + 1);

        TaskRun saved3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        assertThat(saved3.getStatus(), is(TaskRunStatus.BLOCKED));

        TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskRun1.getId() + 1, null);
        eventBus.post(failedEvent);

        awaitUntilAttemptRunning(taskRun3.getId() + 1);

    }

    //scenario: task 0>>1; taskrun 0>>1; 2>>3 (0>>1 is 2>>3's predecessor)
    // task 0 & 1 are WAIT_PREDECESSOR_DOWNSTREAM
    // now, 0 is success, 1 is running, 2 is blocked by 1, 3 is created
    // when 1 is success, 2 is running, 3 should be re-processed as created
    @Test
    public void multiBlockTasks() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList0 = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        List<TaskRun> taskRunList1 = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");

        //prepare task
        Task task0 = taskList.get(0).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR_DOWNSTREAM)).build();
        Task task1 = taskList.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR_DOWNSTREAM)).build();
        taskDao.create(task0);
        taskDao.create(task1);
        //prepare task run 0 & 1
        TaskRun taskRun0 = taskRunList0.get(0);
        TaskRun taskRun1 = taskRunList0.get(1);
        taskRunDao.createTaskRun(taskRun0);
        taskRunDao.createTaskRun(taskRun1);
        //prepare task run 2 with two block conditions
        TaskRunCondition taskRunCondition0 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun0.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH).withResult(false).build();
        TaskRunCondition taskRunCondition1 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH).withResult(false).build();
        TaskRun taskRun2 = taskRunList1.get(0).cloneBuilder()
                .withTaskRunConditions(Arrays.asList(taskRunCondition0, taskRunCondition1)).build();
        TaskRunCondition taskRunCondition2 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH).withResult(false).build();
        TaskRunCondition taskRunCondition3 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun2.getId().toString())))
                .withType(ConditionType.TASKRUN_DEPENDENCY_SUCCESS).withResult(false).build();
        TaskRun taskRun3 = taskRunList1.get(1).cloneBuilder()
                .withTaskRunConditions(Arrays.asList(taskRunCondition2, taskRunCondition3)).build();
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            if (taskAttempt.getTaskRun().getId().equals(taskRun0.getId()) || taskAttempt.getTaskRun().getId().equals(taskRun2.getId())) {
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        //process
        taskManager.submit(Arrays.asList(taskRun0, taskRun1, taskRun2, taskRun3));
        awaitUntilAttemptRunning(taskRun1.getId() + 1);

        TaskRun saved3 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        assertThat(saved3.getStatus(), is(TaskRunStatus.BLOCKED));

        TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskRun1.getId() + 1, null);
        eventBus.post(checkEvent);
        TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskRun1.getId() + 1, null);
        eventBus.post(checkSuccess);

        awaitUntilAttemptRunning(taskRun3.getId() + 1);

        TaskAttemptProps attempt3 = taskRunDao.fetchLatestTaskAttempt(taskRun3.getId());
        assertThat(attempt3.getStatus(), is(TaskRunStatus.RUNNING));
    }

    @Test
    public void retryTaskRunRecoverDownStream() {

        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1");

        long operatorId = taskList.get(0).getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("testOperator")
                .withPackagePath(compileJar(NopOperator.class, NopOperator.class.getSimpleName()))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);

        awaitUntilAttemptDone(taskRun1.getId() + 1);

        // verify update downStream
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(1));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps1.getLogPath(), is(nullValue()));
        assertThat(attemptProps1.getStartAt(), is(notNullValue()));
        assertThat(attemptProps1.getEndAt(), is(notNullValue()));

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        assertThat(attemptProps2.getLogPath(), is(nullValue()));
        assertThat(attemptProps2.getStartAt(), is(nullValue()));
        assertThat(attemptProps2.getEndAt(), is(nullValue()));
        assertThat(taskRunDao.getTermAtOfTaskRun(taskRun2.getId()), is(notNullValue()));

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());


        taskManager.retry(taskRunDao.fetchTaskRunById(taskRun1.getId()).get());


        // verify invoke downStream
        awaitUntilAttemptDone(taskRun2.getId() + 1);

        attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getId(), is(attemptProps2.getId()));
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps2.getLogPath(), is(nullValue()));
        assertThat(attemptProps2.getStartAt(), is(notNullValue()));
        assertThat(attemptProps2.getEndAt(), is(notNullValue()));
        assertThat(taskRunDao.getTermAtOfTaskRun(taskRun2.getId()), is(notNullValue()));

    }

    @Test
    public void updateDownstreamShouldNotEffectOthersDependencies() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2");
        for (Task task : taskList) {
            taskDao.create(task);
        }
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        //taskRun1 run failed
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);


        awaitUntilAttemptDone(taskRun2.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun3.getId() + 1);

        // verify update downStream
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(1));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps1.getLogPath(), is(nullValue()));
        assertThat(attemptProps1.getStartAt(), is(notNullValue()));
        assertThat(attemptProps1.getEndAt(), is(notNullValue()));

        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps2.getLogPath(), is(nullValue()));
        assertThat(attemptProps2.getStartAt(), is(notNullValue()));
        assertThat(attemptProps2.getEndAt(), is(notNullValue()));

        awaitUntilAttemptUpstreamFailed(taskRun3.getId() + 1);
        TaskAttemptProps attemptProps3 = taskRunDao.fetchLatestTaskAttempt(taskRun3.getId());
        assertThat(attemptProps3.getAttempt(), is(1));
        assertThat(attemptProps3.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        assertThat(attemptProps3.getLogPath(), is(nullValue()));
        assertThat(attemptProps3.getStartAt(), is(nullValue()));
        assertThat(attemptProps3.getEndAt(), is(nullValue()));

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        //retry taskRun1
        taskManager.retry(taskRunDao.fetchTaskRunById(taskRun1.getId()).get());

        // verify invoke downStream
        awaitUntilAttemptDone(taskRun3.getId() + 1);

        attemptProps3 = taskRunDao.fetchLatestTaskAttempt(taskRun3.getId());
        assertThat(attemptProps3.getId(), is(attemptProps3.getId()));
        assertThat(attemptProps3.getAttempt(), is(1));
        assertThat(attemptProps3.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps3.getLogPath(), is(nullValue()));
        assertThat(attemptProps3.getStartAt(), is(notNullValue()));
        assertThat(attemptProps3.getEndAt(), is(notNullValue()));

    }

    @Test
    public void testUpstreamSuccess_should_invoke_downstream() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        for (Task task : taskList) {
            taskDao.create(task);
        }
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);


        awaitUntilAttemptDone(taskRun1.getId() + 1);

        // verify
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(1));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps1.getLogPath(), is(nullValue()));
        assertThat(attemptProps1.getStartAt(), is(notNullValue()));
        assertThat(attemptProps1.getEndAt(), is(notNullValue()));

        awaitUntilAttemptDone(taskRun2.getId() + 1);
        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps2.getLogPath(), is(nullValue()));
        assertThat(attemptProps2.getStartAt(), is(notNullValue()));
        assertThat(attemptProps2.getEndAt(), is(notNullValue()));

        assertTransitAtLeastOnce(attemptProps1.getId(),
                ASSEMBLED,
                SUBMIT,
                READY,
                COMPLETE,
                CHECK_SUCCESS
        );

        assertTransitAtLeastOnce(attemptProps2.getId(),
                ASSEMBLED,
                WAIT,
                CONDITION_CHANGE,
                ASSEMBLED,
                SUBMIT,
                READY,
                COMPLETE,
                CHECK_SUCCESS);

    }

    @Test
    public void upstreamFailed_should_stop_downstream() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        for (Task task : taskList) {
            taskDao.create(task);
        }
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        //taskRun1 run failed
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_FAILED, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);


        awaitUntilAttemptDone(taskRun1.getId() + 1);

        // verify update downStream
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(1));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.CHECK_FAILED));
        assertThat(attemptProps1.getLogPath(), is(nullValue()));
        assertThat(attemptProps1.getStartAt(), is(notNullValue()));
        assertThat(attemptProps1.getEndAt(), is(notNullValue()));

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        assertThat(attemptProps2.getLogPath(), is(nullValue()));
        assertThat(attemptProps2.getStartAt(), is(nullValue()));
        assertThat(attemptProps2.getEndAt(), is(nullValue()));

        assertTransitAtLeastOnce(attemptProps1.getId(),
                ASSEMBLED,
                SUBMIT,
                READY,
                COMPLETE,
                CHECK_SUCCESS
        );

        assertTransitAtLeastOnce(attemptProps2.getId(),
                ASSEMBLED,
                WAIT,
                CONDITION_CHANGE,
                ASSEMBLED,
                UPSTREAM_FAILED);

    }

    @Test
    public void multiThreadSubmitShouldNotDuplicate() {
        Set<Long> submittedTaskRun = new HashSet<>();
        List<TaskRun> readyTaskRuns = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Task task = MockTaskFactory.createTask();
            taskDao.create(task);
            TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
            taskRunDao.createTaskRun(taskRun);
            readyTaskRuns.add(taskRun);
        }
        ArgumentCaptor<TaskAttempt> captor = ArgumentCaptor.forClass(TaskAttempt.class);
        doAnswer(invocation -> {
            Thread.sleep(1000);
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            if (!submittedTaskRun.add(taskAttempt.getTaskRun().getId())) {
                throw new IllegalStateException("taskAttemptId = " + taskAttempt.getId() + "is running");
            }
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(readyTaskRuns);
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                Task task = MockTaskFactory.createTask();
                taskDao.create(task);
                TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
                taskRunDao.createTaskRun(taskRun);
                taskManager.submit(Arrays.asList(taskRun));
            });
            thread.start();
        }
        await().atMost(60, TimeUnit.SECONDS).until(() ->
                submittedTaskRun.size() == 8
        );
        verify(executor, times(8)).submit(captor.capture());

    }


    @Test
    public void testCreateAttemptWhenTaskRunUpstreamFailedShouldUpstreamFailed() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.
                createTaskRun(task).cloneBuilder().withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .build();
        taskManager.submit(Arrays.asList(taskRun));

        //verify create attempt
        TaskAttemptProps created = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(created.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
    }

    @Test
    public void testReSubmitTaskAttempt_AttemptTimesShouldInvariant() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        ArgumentCaptor<TaskAttempt> captor = ArgumentCaptor.forClass(TaskAttempt.class);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskManager.submit(Arrays.asList(taskRun));
        // verify first submit
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        verify(executor).submit(captor.capture());
        TaskAttempt taskAttempt = captor.getValue();
        assertThat(taskAttempt.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskAttempt.getId(), is(taskRun.getId() + 1));
        assertThat(taskAttempt.getAttempt(), is(1));

        taskManager.submit(Arrays.asList(taskRun));
        // verify resubmit
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);
        TaskAttempt reSubmitAttempt = captor.getValue();
        assertThat(reSubmitAttempt.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(reSubmitAttempt.getId(), is(taskRun.getId() + 1));
        assertThat(reSubmitAttempt.getAttempt(), is(1));
    }

    @Test
    public void retryOldTaskRun_shouldExecute() {
        //prepare old taskRun
        DateTimeUtils.freezeAt("202101010000");
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(failedEvent);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun));

        awaitUntilAttemptDone(taskRun.getId() + 1);

        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(attemptProps1.getAttempt(), is(1));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(attemptProps1.getStartAt(), is(notNullValue()));
        assertThat(attemptProps1.getEndAt(), is(notNullValue()));

        DateTimeUtils.freezeAt("202107010000");

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        //retry taskRun
        TaskRun oldTaskRun = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        taskManager.retry(oldTaskRun);

        //verify
        awaitUntilAttemptDone(taskRun.getId() + 2);

        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(attemptProps2.getAttempt(), is(2));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(attemptProps2.getStartAt(), is(notNullValue()));
        assertThat(attemptProps2.getEndAt(), is(notNullValue()));

        DateTimeUtils.resetClock();
    }

    //scenario: up&down stream: task0>>1;  (taskRun0>>1) is (taskRun2>>3) predecessor
    // current status: taskRun0 success, 1 running, 2 success
    // taskRun3 need to wait predecessor 1 finish
    // when 3 created, 3 should be blocked and 1 are 5's conditions with correct config
    @Test
    public void testTaskRunBlockByPredecessor_createSuccess() {
        //prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, 1l, "0>>1");
        Task task0 = tasks.get(0);
        Task task1 = tasks.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build();
        TaskRun taskRun0 = MockTaskRunFactory.createTaskRun(task0).cloneBuilder().withStatus(TaskRunStatus.SUCCESS).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1).cloneBuilder()
                .withStatus(TaskRunStatus.RUNNING)
                .withTaskRunConditions(Lists.newArrayList(createCondition(taskRun0, ConditionType.TASKRUN_DEPENDENCY_SUCCESS, true)))
                .withDependentTaskRunIds(Lists.newArrayList(taskRun0.getId()))
                .build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task0).cloneBuilder()
                .withStatus(TaskRunStatus.SUCCESS)
                .build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task1).cloneBuilder()
                .withTaskRunConditions(Lists.newArrayList(
                        createCondition(taskRun1, ConditionType.TASKRUN_PREDECESSOR_FINISH, false),
                        createCondition(taskRun2, ConditionType.TASKRUN_DEPENDENCY_SUCCESS, true)))
                .withDependentTaskRunIds(Lists.newArrayList(taskRun2.getId()))
                .build();
        ;
        taskDao.create(task0);
        taskDao.create(task1);
        taskRunDao.createTaskRun(taskRun0);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        taskManager.submit(Lists.newArrayList(taskRun3));

        awaitUntilAttemptBlock(taskRun3.getId() + 1);

        // verify
        TaskRun submitted3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        assertThat(submitted3.getStatus(), is(TaskRunStatus.BLOCKED));
    }

    //scenario: up&down stream: task0>>1;0>>2;  taskRun3 is 0' predecessor
    // current status: taskRun 0 success, 1 success, 2 running
    // 3 need to wait predecessor downstream 1&2 finish
    // when 3 created, 3 should be blocked and 2 are 3's condition with correct config
    @Test
    public void testTaskRunBlockByPredecessorDownstream_createSuccess() {
        //prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(3, 1l, "0>>1;0>>2");
        Task task0 = tasks.get(0).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR_DOWNSTREAM))
                .build();
        Task task1 = tasks.get(1);
        Task task2 = tasks.get(2);
        TaskRun taskRun0 = MockTaskRunFactory.createTaskRun(task0)
                .cloneBuilder().withStatus(TaskRunStatus.SUCCESS).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder()
                .withTaskRunConditions(Lists.newArrayList(createCondition(taskRun0, ConditionType.TASKRUN_DEPENDENCY_SUCCESS, true)))
                .withDependentTaskRunIds(Lists.newArrayList(taskRun0.getId()))
                .withStatus(TaskRunStatus.SUCCESS).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2)
                .cloneBuilder()
                .withTaskRunConditions(Lists.newArrayList(createCondition(taskRun0, ConditionType.TASKRUN_DEPENDENCY_SUCCESS, true)))
                .withDependentTaskRunIds(Lists.newArrayList(taskRun0.getId()))
                .withStatus(TaskRunStatus.RUNNING).build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task0).cloneBuilder()
                .withTaskRunConditions(Lists.newArrayList(
                        createCondition(taskRun0, ConditionType.TASKRUN_PREDECESSOR_FINISH, true),
                        createCondition(taskRun1, ConditionType.TASKRUN_PREDECESSOR_FINISH, true),
                        createCondition(taskRun2, ConditionType.TASKRUN_PREDECESSOR_FINISH, false)))
                .build();
        taskDao.create(task0);
        taskDao.create(task1);
        taskDao.create(task2);
        taskRunDao.createTaskRun(taskRun0);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        taskManager.submit(Lists.newArrayList(taskRun3));

        awaitUntilAttemptBlock(taskRun3.getId() + 1);

        // verify
        TaskRun submitted3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        assertThat(submitted3.getStatus(), is(TaskRunStatus.BLOCKED));

    }

    //scenario: task 0; task run 0 is 1's predecessor, 1 is 2's predecessor
    // 0 is running, 1 is blocked when spawn 2 , 2's status should be BLOCKED
    @Test
    public void multiBlocked_shouldCreateWithBlocked() {
        Task task = MockTaskFactory.createTask(1l).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build();
        TaskRun taskRun0 = MockTaskRunFactory.createTaskRun(task);
        TaskRunCondition taskRunCondition = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun0.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .withResult(false)
                .build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task)
                .cloneBuilder()
                .withTaskRunConditions(Collections.singletonList(taskRunCondition)).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withTaskRunConditions(Lists.newArrayList(createCondition(taskRun1, ConditionType.TASKRUN_PREDECESSOR_FINISH, false)))
                .build();
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun0);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Lists.newArrayList(taskRun0, taskRun1, taskRun2));

        awaitUntilAttemptRunning(taskRun0.getId() + 1);

        // verify
        TaskRun submitted1 = taskRunDao.fetchTaskRunById(taskRun0.getId()).get();
        TaskRun submitted2 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        TaskRun submitted3 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();

        assertThat(submitted1.getStatus(), is(TaskRunStatus.RUNNING));
        assertThat(submitted2.getStatus(), is(TaskRunStatus.BLOCKED));
        assertThat(submitted3.getStatus(), is(TaskRunStatus.BLOCKED));

    }

    /**
     * taskRun1 is taskRun3's upstream
     * taskRun2、3 is taskRun4's upstream
     * taskRun3 is taskRun5's upstream
     * taskRun1、2 failed
     * retry taskRun1, taskRun3、5 should be invoke,taskRun4 should still upstreamFailed
     */
    @Test
    public void retryOneOfFailedUpstream_task_run_status_should_still_upstream_failed() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(5, "0>>2;1>>3;2>>3;2>>4");
        long operatorId = taskList.get(0).getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("testOperator")
                .withPackagePath(compileJar(NopOperator.class, NopOperator.class.getSimpleName()))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>3;2>>3;2>>4");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        TaskRun taskRun4 = taskRunList.get(3);
        TaskRun taskRun5 = taskRunList.get(4);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);
        taskRunDao.createTaskRun(taskRun5);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1) || taskAttemptId.equals(taskRun2.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);
        // wait taskRun1,taskRun2 failed
        awaitUntilAttemptDone(taskRun1.getId() + 1);
        awaitUntilAttemptDone(taskRun2.getId() + 1);

        //retry taskRun1
        TaskRun saved1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        taskManager.retry(saved1);

        //wait taskRun5 retry finished
        awaitUntilAttemptDone(taskRun5.getId() + 1);

        //verify
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(2));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.SUCCESS));
        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.FAILED));
        TaskAttemptProps attemptProps3 = taskRunDao.fetchLatestTaskAttempt(taskRun3.getId());
        assertThat(attemptProps3.getAttempt(), is(1));
        assertThat(attemptProps3.getStatus(), is(TaskRunStatus.SUCCESS));
        TaskAttemptProps attemptProps4 = taskRunDao.fetchLatestTaskAttempt(taskRun4.getId());
        assertThat(attemptProps4.getAttempt(), is(1));
        assertThat(attemptProps4.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        TaskAttemptProps attemptProps5 = taskRunDao.fetchLatestTaskAttempt(taskRun5.getId());
        assertThat(attemptProps5.getAttempt(), is(1));
        assertThat(attemptProps5.getStatus(), is(TaskRunStatus.SUCCESS));
    }

    /**
     * taskRun1、2 is taskRun3's upstream
     * <p>
     * taskRun1、2 failed
     * retry taskRun1, taskRun3 should be upstreamFailed
     */
    @Test
    public void retryOneOfFailedUpstream_downstream_should_still_upstream_failed() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2");
        long operatorId = taskList.get(0).getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .withClassName("testOperator")
                .withPackagePath(compileJar(NopOperator.class, NopOperator.class.getSimpleName()))
                .build();
        operatorDao.createWithId(op, operatorId);
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1) || taskAttemptId.equals(taskRun2.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(taskRunList);
        // wait taskRun1,taskRun2 failed
        awaitUntilAttemptDone(taskRun1.getId() + 1);
        awaitUntilAttemptDone(taskRun2.getId() + 1);

        //retry taskRun1
        TaskRun saved1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        taskManager.retry(saved1);

        //wait taskRun1 retry finished
        awaitUntilAttemptDone(taskRun1.getId() + 2);
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

        //verify
        TaskAttemptProps attemptProps1 = taskRunDao.fetchLatestTaskAttempt(taskRun1.getId());
        assertThat(attemptProps1.getAttempt(), is(2));
        assertThat(attemptProps1.getStatus(), is(TaskRunStatus.SUCCESS));
        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getAttempt(), is(1));
        assertThat(attemptProps2.getStatus(), is(TaskRunStatus.FAILED));
        TaskAttemptProps attemptProps3 = taskRunDao.fetchLatestTaskAttempt(taskRun3.getId());
        assertThat(attemptProps3.getAttempt(), is(1));
        assertThat(attemptProps3.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
    }

    //scenario: 1>>2
    // 1 fail, 2-> upstream_failed
    // skip 1, 2: created->success
    @Test
    public void skipFailedTaskRun_downstreamShouldBeCreated() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        taskList.forEach(x -> taskDao.create(x));
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(Arrays.asList(taskRun1, taskRun2));

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        TaskRun submitted1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        MatcherAssert.assertThat(submitted1.getStatus(), is(TaskRunStatus.FAILED));
        TaskRun submitted2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        MatcherAssert.assertThat(submitted2.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));

        taskManager.skip(submitted1);
        awaitUntilAttemptDone(taskRun2.getId() + 1);

        submitted2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        MatcherAssert.assertThat(submitted2.getStatus(), is(TaskRunStatus.SUCCESS));
    }

    // scenario: taskrun 1>>2 3>>4, 2 is 4's predecessor respectively
    // when 1 fail & 3 running, 2 is upstream_fail and 4 is created
    // skip 1, 2 able to run: created->running, 4 is blocked by 2
    @Test
    public void skipFailedTaskRun_downstreamSuccessorShouldBeBlocked() throws InterruptedException {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build();

        List<TaskRun> taskRunList1 = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        List<TaskRun> taskRunList2 = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        TaskRun taskRun1 = taskRunList1.get(0);
        TaskRun taskRun2 = taskRunList1.get(1);
        TaskRun taskRun3 = taskRunList2.get(0);
        TaskRun taskRun4 = taskRunList2.get(1).cloneBuilder()
                .withTaskRunConditions(Arrays.asList(createCondition(taskRun2, ConditionType.TASKRUN_PREDECESSOR_FINISH, false),
                        createCondition(taskRun3, ConditionType.TASKRUN_DEPENDENCY_SUCCESS, false))).build();
        taskDao.create(task1);
        taskDao.create(task2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else if (taskAttemptId.equals(taskRun2.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
            } else if (taskAttemptId.equals(taskRun3.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun1, taskRun2, taskRun3, taskRun4));

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        awaitUntilAttemptRunning(taskRun3.getId() + 1);
        TimeUnit.SECONDS.sleep(2);

        TaskRun submitted1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        MatcherAssert.assertThat(submitted1.getStatus(), is(TaskRunStatus.FAILED));
        TaskRun submitted2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        MatcherAssert.assertThat(submitted2.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        TaskRun submitted3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        MatcherAssert.assertThat(submitted3.getStatus(), is(TaskRunStatus.RUNNING));
        TaskRun submitted4 = taskRunDao.fetchTaskRunById(taskRun4.getId()).get();
        MatcherAssert.assertThat(submitted4.getStatus(), is(TaskRunStatus.CREATED));

        taskManager.skip(submitted1);
        awaitUntilAttemptRunning(taskRun2.getId() + 1);
        TimeUnit.SECONDS.sleep(2);


        submitted2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        MatcherAssert.assertThat(submitted2.getStatus(), is(TaskRunStatus.RUNNING));
        submitted3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        MatcherAssert.assertThat(submitted3.getStatus(), is(TaskRunStatus.RUNNING));
        submitted4 = taskRunDao.fetchTaskRunById(taskRun4.getId()).get();
        MatcherAssert.assertThat(submitted4.getStatus(), is(TaskRunStatus.BLOCKED));
    }

    @Test
    public void skipNotAllowedTaskRun_shouldThrowException() {
        TaskRun taskRun = MockTaskRunFactory.createTaskRun().cloneBuilder()
                .withStatus(TaskRunStatus.RUNNING).build();
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(MockTaskAttemptFactory.createTaskAttempt(taskRun));

        assertThrows("This taskRun is not allowed to skip. Status not match", IllegalStateException.class, () -> taskManager.skip(taskRun));
    }

    /**
     * scenario: 1>>2, both are created
     * remove 2's dependency 1
     * 2 is satisfied to run
     */
    @Test
    public void removeAllTaskRunDependency_should_make_it_able_to_run() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun1, taskRun2));

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);

        //process
        taskManager.batchRemoveDependency(taskRun2.getId(), Lists.newArrayList(taskRun1.getId()));

        awaitUntilAttemptDone(taskRun2.getId() + 1);

        //verify
        TaskRun saved1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        TaskRun saved2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        assertThat(saved1.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(saved1.getStartAt(), is(notNullValue()));
        assertThat(saved1.getEndAt(), is(notNullValue()));
        assertThat(saved2.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(saved2.getStartAt(), is(notNullValue()));
        assertThat(saved2.getEndAt(), is(notNullValue()));
    }


    /**
     * scenario: 1>>3, 2>>3
     * 1 fail, 2 running -> 3 upstream_failed
     * remove 3's dependency 1, 3 -> created
     */
    @Test
    public void removePartOfTaskRunDependency_should_not_able_to_run() throws InterruptedException {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else if (taskAttempt.getId().equals(taskRun2.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun1, taskRun2, taskRun3));

        awaitUntilAttemptUpstreamFailed(taskRun3.getId() + 1);
        //process
        taskManager.batchRemoveDependency(taskRun3.getId(), Lists.newArrayList(taskRun1.getId()));

        TimeUnit.SECONDS.sleep(2);

        //verify
        TaskRun saved1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        TaskRun saved2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        assertThat(saved1.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(saved1.getStartAt(), is(notNullValue()));
        assertThat(saved1.getEndAt(), is(notNullValue()));
        assertThat(saved2.getStatus(), is(TaskRunStatus.RUNNING));
        assertThat(saved2.getStartAt(), is(notNullValue()));
        assertThat(saved2.getEndAt(), is(nullValue()));
        TaskRun savedTaskRun3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        assertThat(savedTaskRun3.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(savedTaskRun3.getFailedUpstreamTaskRunIds().size(), is(0));
        assertThat(savedTaskRun3.getTermAt(), is(nullValue()));
    }

    /**
     * scenario: taskrun 1>>2>>3, taskrun 3 is 4's predecessor
     * 1 fail, 2 & 3 upstream_failed, 4 created
     * remove 2's dependency 1, 2-> able to run,3->created, 4->blocked by 3
     */
    @Test
    public void removeDependencyWithMultiDownstream() throws InterruptedException {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2");
        taskList.set(2, taskList.get(2).cloneBuilder().
                withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, CRON_EVERY_MINUTE, ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build());
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>2");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        ;
        TaskRun taskRun3 = taskRunList.get(2);
        TaskRun taskRun4 = MockTaskRunFactory.createTaskRun(taskList.get(2)).cloneBuilder()
                .withTaskRunConditions(Collections.singletonList(createCondition(taskRun3, ConditionType.TASKRUN_PREDECESSOR_FINISH, false)))
                .build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(Arrays.asList(taskRun1, taskRun2, taskRun3));
        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);

        //process
        taskManager.batchRemoveDependency(taskRun2.getId(), Collections.singletonList(taskRun1.getId()));
        TimeUnit.SECONDS.sleep(2);

        taskManager.submit(Arrays.asList(taskRun4));
        TimeUnit.SECONDS.sleep(1);


        //verify
        TaskRun savedTaskRun2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();
        MatcherAssert.assertThat(savedTaskRun2.getFailedUpstreamTaskRunIds().size(), is(0));
        MatcherAssert.assertThat(savedTaskRun2.getStatus(), is(TaskRunStatus.RUNNING));
        TaskRun savedTaskRun3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        MatcherAssert.assertThat(savedTaskRun3.getFailedUpstreamTaskRunIds().size(), is(0));
        MatcherAssert.assertThat(savedTaskRun3.getStatus(), is(TaskRunStatus.CREATED));
        TaskRun savedTaskRun4 = taskRunDao.fetchTaskRunById(taskRun4.getId()).get();
        MatcherAssert.assertThat(savedTaskRun4.getStatus(), is(TaskRunStatus.BLOCKED));
    }

    /**
     * scenario: 1>>2, 2>>5, 3>>5, 4>>5, 5>>6
     * 1,3,4 are failed, 5 is upstream_failed with root cause 1,3,4; 6 is upstream_failed with root cause 1,3,4
     * remove 5's dependency 2&3;
     * 5,6 should keep as upstream_failed with root cause 4
     */
    @Test
    public void removePartialFailedDependency_shouldSuccess() throws InterruptedException {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(6, "0>>1;1>>4;2>>4;3>>4;4>>5");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>4;2>>4;3>>4;4>>5");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        TaskRun taskRun4 = taskRunList.get(3);
        TaskRun taskRun5 = taskRunList.get(4);
        TaskRun taskRun6 = taskRunList.get(5);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);
        taskRunDao.createTaskRun(taskRun5);
        taskRunDao.createTaskRun(taskRun6);
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (!taskAttemptId.equals(taskRun5.getId() + 1) || !taskAttemptId.equals(taskRun6.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun1, taskRun2, taskRun3, taskRun4, taskRun5, taskRun6));
        awaitUntilAttemptDone(taskRun3.getId() + 1);
        awaitUntilAttemptDone(taskRun4.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun6.getId() + 1);

        //process
        taskManager.batchRemoveDependency(taskRun5.getId(), Arrays.asList(taskRun2.getId(), taskRun3.getId()));

        //verify
        TaskRun savedTaskRun5 = taskRunDao.fetchTaskRunById(taskRun5.getId()).get();
        TaskRun savedTaskRun6 = taskRunDao.fetchTaskRunById(taskRun6.getId()).get();
        MatcherAssert.assertThat(savedTaskRun5.getFailedUpstreamTaskRunIds().size(), is(1));
        MatcherAssert.assertThat(savedTaskRun5.getFailedUpstreamTaskRunIds().get(0), is(taskRun4.getId()));
        MatcherAssert.assertThat(savedTaskRun5.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        MatcherAssert.assertThat(savedTaskRun6.getFailedUpstreamTaskRunIds().size(), is(1));
        MatcherAssert.assertThat(savedTaskRun6.getFailedUpstreamTaskRunIds().get(0), is(taskRun4.getId()));
        MatcherAssert.assertThat(savedTaskRun6.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
    }

    /**
     * scenario: 1>>2, 1>>3, 2,3>>4
     * 1 is failed, 2,3,4 are upstream_failed
     * remove 4's dependency 2, 4 keep upstream_failed with upstream_failed_taskrun_id 1.
     */
    @Test
    public void removeDependencyWithSameRootCause() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;0>>2;1>>3;2>>3");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;0>>2;1>>3;2>>3");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        TaskRun taskRun4 = taskRunList.get(3);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            Long taskAttemptId = taskAttempt.getId();
            if (taskAttemptId.equals(taskRun1.getId() + 1)) {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                TaskRunTransitionEvent failedEvent = new TaskRunTransitionEvent(FAILED, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
                eventBus.post(failedEvent);
            } else {
                TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
                eventBus.post(runningEvent);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskManager.submit(Arrays.asList(taskRun1, taskRun2, taskRun3, taskRun4));

        awaitUntilAttemptUpstreamFailed(taskRun2.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun3.getId() + 1);
        awaitUntilAttemptUpstreamFailed(taskRun4.getId() + 1);

        //process
        taskManager.batchRemoveDependency(taskRun4.getId(), Arrays.asList(taskRun2.getId()));

        //verify
        TaskRun savedTaskRun4 = taskRunDao.fetchTaskRunById(taskRun4.getId()).get();
        MatcherAssert.assertThat(savedTaskRun4.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
        MatcherAssert.assertThat(savedTaskRun4.getFailedUpstreamTaskRunIds().size(), is(1));
        MatcherAssert.assertThat(savedTaskRun4.getFailedUpstreamTaskRunIds().get(0), is(taskRun1.getId()));

    }

    @Test
    public void submitTaskRunWithLossConditionUpdate_should_be_trigger() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2;");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2;");
        TaskRun taskRun1 = taskRunList.get(0).cloneBuilder()
                .withStatus(TaskRunStatus.CREATED).build();
        TaskRun taskRun2 = taskRunList.get(1).cloneBuilder()
                .withStatus(TaskRunStatus.CREATED).build();
        TaskRun taskRun3 = taskRunList.get(2).cloneBuilder().withStatus(TaskRunStatus.CREATED).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());
        taskManager.submit(Arrays.asList(taskRun1, taskRun2));

        //wait upstream finished
        awaitUntilAttemptDone(taskRun1.getId() + 1);
        awaitUntilAttemptDone(taskRun2.getId() + 1);

        taskManager.submit(Arrays.asList(taskRun3));

        awaitUntilAttemptDone(taskRun3.getId() + 1);

    }

    @Test
    public void blockByPredecessorDownstream_predecessor_finished_should_still_block() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2;");
        Task task1 = taskList.get(0).cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR_DOWNSTREAM))
                .build();
        taskDao.create(task1);
        taskDao.create(taskList.get(1));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2;");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRunCondition taskRunCondition1 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .withResult(false).build();
        TaskRunCondition taskRunCondition2 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun2.getId().toString())))
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .withResult(false).build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder().withTaskRunConditions(Arrays.asList(taskRunCondition1, taskRunCondition2)).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            if (taskAttempt.getTaskRun().getId().equals(taskRun1.getId())) {
                TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
                TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
                eventBus.post(checkEvent);
                eventBus.post(checkSuccess);
            }
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        //process
        taskManager.submit(Arrays.asList(taskRun1, taskRun2, taskRun3));
        awaitUntilAttemptRunning(taskRun2.getId() + 1);
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);

        //verify
        TaskRun saved3 = taskRunDao.fetchTaskRunById(taskRun3.getId()).get();
        assertThat(saved3.getStatus(), is(TaskRunStatus.BLOCKED));
    }

    @ParameterizedTest
    @MethodSource("recoverTaskRun")
    public void recoverStateMachineWithoutUnCompletedEvent(Integer taskRunPhase) {
        //prepare
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRunWithPhase(task, taskRunPhase);
        TaskAttempt savedAttempt = MockTaskAttemptFactory.createTaskAttemptWithPhase(taskRun, taskRunPhase);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(savedAttempt);
        taskRunDao.updateTaskAttemptPhase(savedAttempt.getId(), taskRunPhase, null, null, null, null);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskRunStateMachineDispatcher.reset();
        //process
        taskRunStateMachineDispatcher.recover();

        //verify
        awaitUntilAttemptDone(savedAttempt.getId());
        savedAttempt = taskRunDao.fetchAttemptById(savedAttempt.getId()).get();
        assertThat(savedAttempt.getStatus(), is(TaskRunStatus.SUCCESS));

    }

    @ParameterizedTest
    @MethodSource("recoverTaskRunWithEvent")
    public void recoverStateMachineWithUnCompletedEvent(Integer taskRunPhase, List<TaskRunTransitionEventType> unCompletedEvents) {
        //prepare
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRunWithPhase(task, taskRunPhase);
        TaskAttempt savedAttempt = MockTaskAttemptFactory.createTaskAttemptWithPhase(taskRun, taskRunPhase);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(savedAttempt);
        taskRunDao.updateTaskAttemptPhase(savedAttempt.getId(), taskRunPhase, null, null, null, null);
        for (TaskRunTransitionEventType eventType : unCompletedEvents) {
            TaskRunTransitionEvent unCompletedEvent = TaskRunTransitionEvent
                    .newBuilder()
                    .withTaskAttemptId(savedAttempt.getId())
                    .withType(eventType)
                    .build();
            taskRunDao.saveTransitEvent(unCompletedEvent, false);
        }

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        taskRunStateMachineDispatcher.reset();
        //process
        taskRunStateMachineDispatcher.recover();

        //verify
        awaitUntilAttemptDone(savedAttempt.getId());
        savedAttempt = taskRunDao.fetchAttemptById(savedAttempt.getId()).get();
        assertThat(savedAttempt.getStatus(), is(TaskRunStatus.SUCCESS));

    }

    @Test
    public void taskRunCreatedReceiveConditionChange_should_not_discard(){
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1");
        for (TaskRun taskRun : taskRuns) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }
        TaskRun taskRun1 = taskRuns.get(0);
        TaskRun taskRun2 = taskRuns.get(1);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            TaskRunTransitionEvent runningEvent = new TaskRunTransitionEvent(READY, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkEvent = new TaskRunTransitionEvent(COMPLETE, taskAttempt.getId(), null);
            TaskRunTransitionEvent checkSuccess = new TaskRunTransitionEvent(CHECK_SUCCESS, taskAttempt.getId(), null);
            eventBus.post(runningEvent);
            eventBus.post(checkEvent);
            eventBus.post(checkSuccess);
            return null;
        }).when(executor).submit(ArgumentMatchers.any());


        TaskAttempt attempt2 = MockTaskAttemptFactory.createTaskAttemptWithPhase(taskRun2,TaskRunPhase.CREATED);
        taskRunDao.createAttempt(attempt2);
        taskManager.submit(Arrays.asList(taskRun1));

        awaitUntilAttemptDone(attempt2.getId());

        assertTransitAtLeastOnce(attempt2.getId(),
                CONDITION_CHANGE,
                ASSEMBLED,
                SUBMIT,
                READY,
                COMPLETE,
                CHECK_SUCCESS);
    }

    public static Stream<Integer> recoverTaskRun() {
        return Stream.of(TaskRunPhase.CREATED, TaskRunPhase.CREATED | TaskRunPhase.WAITING, TaskRunPhase.CREATED | TaskRunPhase.WAITING | TaskRunPhase.QUEUED);
    }

    public static Stream<Arguments> recoverTaskRunWithEvent() {
        return Stream.of(Arguments.of(TaskRunPhase.CREATED, Lists.newArrayList(ASSEMBLED)),
                Arguments.of(TaskRunPhase.CREATED | TaskRunPhase.WAITING, Lists.newArrayList(SUBMIT)));
    }

    private TaskRunCondition createCondition(TaskRun taskRun, ConditionType conditionType, boolean result) {
        TaskRunCondition taskRunCondition = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun.getId().toString())))
                .withType(conditionType)
                .withResult(result).build();
        return taskRunCondition;
    }


    private TaskAttemptStatusChangeEvent prepareEvent(long taskAttemptId, String taskName, long taskId, TaskRunStatus from, TaskRunStatus to) {
        return new TaskAttemptStatusChangeEvent(taskAttemptId, from, to, taskName, taskId);
    }

    private String compileJar(Class<? extends KunOperator> operatorClass, String operatorClassName) {
        return OperatorCompiler.compileJar(operatorClass, operatorClassName);
    }

    private boolean invoked() {
        return !Mockito.mockingDetails(executor).getInvocations().isEmpty();
    }

    private List<TaskAttempt> getSubmittedTaskAttempts() {
        ArgumentCaptor<TaskAttempt> captor = ArgumentCaptor.forClass(TaskAttempt.class);
        verify(executor).submit(captor.capture());
        return captor.getAllValues();
    }

    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
        });
    }

    private void awaitUntilAttemptRunning(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isRunning());
        });
    }

    private void awaitUntilAttemptBlock(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isBlocking());
        });
    }

    private void awaitUntilAttemptUpstreamFailed(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isUpstreamFailed());
        });
    }

    private void assertTransitAtLeastOnce(Long attemptId, TaskRunTransitionEventType... asserts) {
        checkArgument(asserts.length > 1);

        List<Event> events = eventCollector.getEvents();

        List<Event> eventsOfAttempt = events.stream()
                .distinct()
                .filter(e -> e instanceof TaskRunTransitionEvent &&
                        ((TaskRunTransitionEvent) e).getTaskAttemptId().equals(attemptId))
                .collect(Collectors.toList());


        for (int i = 0; i < asserts.length - 1; i++) {
            TaskRunTransitionEvent event = (TaskRunTransitionEvent) eventsOfAttempt.get(i);
            MatcherAssert.assertThat(event.getTaskAttemptId(), Matchers.is(attemptId));
            MatcherAssert.assertThat(event.getType(), Matchers.is(asserts[i]));
        }
    }
}