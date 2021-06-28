package com.miotech.kun.workflow.executor;

import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.executor.local.QueueManage;
import com.miotech.kun.workflow.executor.local.LocalTaskAttemptQueue;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.joor.Reflect;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

import static com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory.createTaskAttempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class QueueManageTest extends GuiceTestBase {

    private final Logger logger = LoggerFactory.getLogger(QueueManageTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testChangeAttemptPriority() {
        QueueManage queueManage = prepareQueueManage(3);
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
        LocalTaskAttemptQueue attemptQueue = queueManage.getTaskAttemptQueue("default");
        Queue queue = Reflect.on(attemptQueue).field("queue").get();
        Object[] attempts = queue.toArray();
        assertThat(((TaskAttempt) attempts[0]).getId(), is(taskAttempt2.getId()));
        assertThat(((TaskAttempt) attempts[1]).getId(), is(taskAttempt1.getId()));
        assertThat(((TaskAttempt) attempts[2]).getId(), is(taskAttempt3.getId()));
        queueManage.changePriority("default", taskAttempt3.getId(), TaskPriority.HIGHEST.getPriority());
        assertThat(attemptQueue.take().getId(), is(taskAttempt3.getId()));
        assertThat(attemptQueue.take().getId(), is(taskAttempt2.getId()));
        assertThat(attemptQueue.take().getId(), is(taskAttempt1.getId()));

    }

    @Test
    public void testSamePriorityShouldFIFO() {
        QueueManage queueManage = prepareQueueManage(3);
        Task task1 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        Task task2 = MockTaskFactory.createTask();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        Task task3 = MockTaskFactory.createTask();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        TaskAttempt taskAttempt3 = createTaskAttempt(taskRun3);
        queueManage.submit(taskAttempt1);
        queueManage.submit(taskAttempt2);
        queueManage.submit(taskAttempt3);

        //verify
        LocalTaskAttemptQueue attemptQueue = queueManage.getTaskAttemptQueue("default");
        assertThat(attemptQueue.take().getId(), is(taskAttempt1.getId()));
        queueManage.release("default", taskAttempt1.getId());
        Task task4 = MockTaskFactory.createTask();
        TaskRun taskRun4 = MockTaskRunFactory.createTaskRun(task4);
        TaskAttempt taskAttempt4 = createTaskAttempt(taskRun4);
        queueManage.submit(taskAttempt4);
        assertThat(attemptQueue.take().getId(), is(taskAttempt2.getId()));
        assertThat(attemptQueue.take().getId(), is(taskAttempt3.getId()));
        assertThat(attemptQueue.take().getId(), is(taskAttempt4.getId()));

    }

    @Test
    public void repeatReleaseTest() {
        //prepare
        QueueManage queueManage = prepareQueueManage(8);
        Task task1 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        queueManage.submit(taskAttempt1);
        Task task2 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(TaskPriority.HIGH.getPriority()).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        queueManage.submit(taskAttempt2);

        try {
            //take taskAttempt from queue
            queueManage.take();
            queueManage.take();
        } catch (InterruptedException e) {
            logger.error("take taskAttempt from queue failed", e);
        }
        //release token once
        queueManage.release(taskAttempt1.getQueueName(), taskAttempt1.getId());

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("token with taskAttemptId = " + taskAttempt1.getId() + "has been released");
        //release token twice should throw exception
        queueManage.release(taskAttempt1.getQueueName(), taskAttempt1.getId());

    }

    @Test
    public void repeatAcquireTest() {
        //prepare
        QueueManage queueManage = prepareQueueManage(8);
        Task task1 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        queueManage.submit(taskAttempt1);
        Task task2 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(TaskPriority.HIGH.getPriority()).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        queueManage.submit(taskAttempt2);

        try {
            //take taskAttempt from queue
            queueManage.take();
            queueManage.take();
        } catch (InterruptedException e) {
            logger.error("take taskAttempt from queue failed", e);
        }
        //release token once
        queueManage.release(taskAttempt1.getQueueName(), taskAttempt1.getId());

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("token with taskAttemptId = " + taskAttempt1.getId() + "has been released");
        //release token twice should throw exception
        queueManage.release(taskAttempt1.getQueueName(), taskAttempt1.getId());

    }

    @Test
    public void releaseTokenUnCorrect() {
        //prepare
        QueueManage queueManage = prepareQueueManage(8);
        Task task1 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        queueManage.submit(taskAttempt1);
        Task task2 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(TaskPriority.HIGH.getPriority()).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);

        try {
            //take taskAttempt from queue
            queueManage.take();
        } catch (InterruptedException e) {
            logger.error("take taskAttempt from queue failed", e);
        }
        //release token with taskAttempt2

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("token with taskAttemptId = " + taskAttempt2.getId() + "has been released");
        queueManage.release(taskAttempt2.getQueueName(), taskAttempt2.getId());

    }

    private QueueManage prepareQueueManage(int defaultCapacity) {
        Props props = new Props();
        props.put("executor.env.resourceQueues", "default,user");
        props.put("executor.env.resourceQueues.default.quota.workerNumbers", defaultCapacity);
        props.put("executor.env.resourceQueues.user.quota.workerNumbers", 0);
        return new QueueManage(props);
    }


}
