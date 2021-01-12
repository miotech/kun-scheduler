package com.miotech.kun.workflow.executor;

import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.executor.local.QueueManage;
import com.miotech.kun.workflow.executor.local.TaskAttemptQueue;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.joor.Reflect;
import org.junit.Test;

import java.util.Queue;

import static com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory.createTaskAttempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class QueueManageTest extends GuiceTestBase {


    @Test
    public void testChangeAttemptPriority() {
        QueueManage queueManage = prepareQueueManage();
        Task task1 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(TaskPriority.MEDIUM.getPriority()).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        Task task2 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(TaskPriority.HIGH.getPriority()).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        Task task3 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(TaskPriority.LOW.getPriority()).build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        TaskAttempt taskAttempt3 = createTaskAttempt(taskRun3);
        queueManage.submit(taskAttempt1);
        queueManage.submit(taskAttempt2);
        queueManage.submit(taskAttempt3);
        TaskAttemptQueue attemptQueue = queueManage.getTaskAttemptQueue("default");
        Queue queue = Reflect.on(attemptQueue).field("queue").get();
        Object[] attempts =  queue.toArray();
        assertThat(((TaskAttempt) attempts[0]).getId(), is(taskAttempt2.getId()));
        assertThat(((TaskAttempt) attempts[1]).getId(), is(taskAttempt1.getId()));
        assertThat(((TaskAttempt) attempts[2]).getId(), is(taskAttempt3.getId()));
        queueManage.changePriority("default", taskAttempt3.getId(), TaskPriority.HIGHEST.getPriority());
        assertThat(attemptQueue.take().getId(), is(taskAttempt3.getId()));
        assertThat(attemptQueue.take().getId(), is(taskAttempt2.getId()));
        assertThat(attemptQueue.take().getId(), is(taskAttempt1.getId()));

    }

    private QueueManage prepareQueueManage() {
        Props props = new Props();
        props.put("executor.queue", "default,user");
        props.put("executor.queue.default.capacity", 3);
        props.put("executor.queue.user.capacity", 0);
        return new QueueManage(props);
    }


}
