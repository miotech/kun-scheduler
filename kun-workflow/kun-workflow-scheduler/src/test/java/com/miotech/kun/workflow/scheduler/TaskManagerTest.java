package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

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

    @Override
    protected void configuration() {
        super.configuration();
        executor = mock(Executor.class);
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
        assertThat(taskAttempt.getTaskRun(), sameBeanAs(taskRun));
        assertThat(taskAttempt.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskAttempt.getLogPath(), is(nullValue()));
        assertThat(taskAttempt.getStartAt(), is(nullValue()));
        assertThat(taskAttempt.getEndAt(), is(nullValue()));

        TaskAttemptProps attemptProps = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        assertThat(attemptProps.getId(), is(taskAttempt.getId()));
        // TODO: non-passed yet
        // assertThat(attemptProps.getTaskName(), is(taskAttempt.getTaskName()));
        // assertThat(attemptProps.getTaskId(), is(taskAttempt.getTaskId()));
        assertThat(attemptProps.getAttempt(), is(1));
        assertThat(attemptProps.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(attemptProps.getLogPath(), is(nullValue()));
        assertThat(attemptProps.getStartAt(), is(nullValue()));
        assertThat(attemptProps.getEndAt(), is(nullValue()));
    }

    @Test
    public void testSubmit_task_upstream_is_success() {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasks(2);
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1");
        for (TaskRun taskRun : taskRuns) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        TaskRun taskRun1 = taskRuns.get(0);
        TaskRun taskRun2 = taskRuns.get(1);

        TaskAttempt attempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        taskRunDao.createAttempt(attempt1);
        taskRunDao.updateTaskAttemptStatus(attempt1.getId(), TaskRunStatus.SUCCESS);

        // process
        taskManager.submit(Lists.newArrayList(taskRun2));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);

        List<TaskAttempt> result = getSubmittedTaskAttempts();
        assertThat(result, is(hasSize(1)));

        TaskAttempt taskAttempt = result.get(0);
        assertThat(taskAttempt.getId(), is(WorkflowIdGenerator.nextTaskAttemptId(taskRun2.getId(), 1)));
        assertThat(taskAttempt.getAttempt(), is(1));
        assertThat(taskAttempt.getTaskRun(), sameBeanAs(taskRun2));
        assertThat(taskAttempt.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskAttempt.getLogPath(), is(nullValue()));
        assertThat(taskAttempt.getStartAt(), is(nullValue()));
        assertThat(taskAttempt.getEndAt(), is(nullValue()));
    }

    @Test
    public void testSubmit_task_upstream_is_failed() throws InterruptedException {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasks(2);
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

    @Test
    public void testSubmit_task_waiting_for_upstream_becomes_success() throws InterruptedException {
        // prepare
        List<Task> tasks = MockTaskFactory.createTasks(2);
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1");
        for (TaskRun taskRun : taskRuns) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        TaskRun taskRun1 = taskRuns.get(0);
        TaskRun taskRun2 = taskRuns.get(1);

        TaskAttempt attempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        taskRunDao.createAttempt(attempt1);
        taskRunDao.updateTaskAttemptStatus(attempt1.getId(), TaskRunStatus.RUNNING);

        // process
        taskManager.submit(Lists.newArrayList(taskRun2));
        TimeUnit.SECONDS.sleep(2);
        assertThat(invoked(), is(false));

        // post successful event
        eventBus.post(new TaskAttemptStatusChangeEvent(attempt1.getId(), TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, taskName, taskId));

        // verify
        await().atMost(10, TimeUnit.SECONDS).until(this::invoked);

        List<TaskAttempt> result = getSubmittedTaskAttempts();
        assertThat(result, is(hasSize(1)));

        TaskAttempt taskAttempt = result.get(0);
        assertThat(taskAttempt.getId(), is(WorkflowIdGenerator.nextTaskAttemptId(taskRun2.getId(), 1)));
        assertThat(taskAttempt.getAttempt(), is(1));
        assertThat(taskAttempt.getTaskRun(), sameBeanAs(taskRun2));
        assertThat(taskAttempt.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(taskAttempt.getLogPath(), is(nullValue()));
        assertThat(taskAttempt.getStartAt(), is(nullValue()));
        assertThat(taskAttempt.getEndAt(), is(nullValue()));
    }

    private boolean invoked() {
        return !Mockito.mockingDetails(executor).getInvocations().isEmpty();
    }

    private List<TaskAttempt> getSubmittedTaskAttempts() {
        ArgumentCaptor<TaskAttempt> captor = ArgumentCaptor.forClass(TaskAttempt.class);
        verify(executor).submit(captor.capture());
        return captor.getAllValues();
    }
}