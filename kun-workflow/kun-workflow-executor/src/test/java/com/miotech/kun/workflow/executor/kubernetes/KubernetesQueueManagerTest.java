package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.publish.NopEventPublisher;
import com.miotech.kun.workflow.executor.TaskAttemptQueue;
import com.miotech.kun.workflow.executor.local.MiscService;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory.createTaskAttempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doAnswer;

public class KubernetesQueueManagerTest extends DatabaseTestBase {

    private KubernetesResourceManager kubernetesResourceManager;

    private KubernetesResourceManager spyManager;

    @Inject
    private MiscService miscService;

    @Inject
    private TaskRunDao taskRunDao;

    @Override
    protected void configuration() {
        super.configuration();
        bind(EventPublisher.class, new NopEventPublisher());
    }

    @Before
    public void init() {
        kubernetesResourceManager = prepareQueueManage();
        spyManager = spy(kubernetesResourceManager);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                TaskAttemptQueue taskAttemptQueue = invocation.getArgument(0, TaskAttemptQueue.class);
                if (taskAttemptQueue.getName().equals("default")) {
                    return true;
                }
                return false;
            }
        }).when(spyManager).hasCapacity(ArgumentMatchers.any());
    }

    private KubernetesResourceManager prepareQueueManage() {
        Props props = new Props();
        props.put("executor.env.resourceQueues", "default,test");
        props.put("executor.env.resourceQueues.default.quota.workerNumbers", 3);
        props.put("executor.env.resourceQueues.test.quota.workerNumbers", 2);
        KubernetesResourceManager queueManager = new KubernetesResourceManager(mock(KubernetesClient.class), props, miscService);
        queueManager.init();
        return queueManager;
    }

    @Test
    public void takeFromQueueHasCapacityShouldReturnAttemptInOrder() {
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt(taskRun2);
        taskRunDao.createAttempt(taskAttempt1);
        taskRunDao.createAttempt(taskAttempt2);
        spyManager.submit(taskAttempt1);
        spyManager.submit(taskAttempt2);
        TaskAttempt queued1 = spyManager.take();
        TaskAttempt queued2 = spyManager.take();
        assertThat(queued1.getId(), is(taskAttempt1.getId()));
        assertThat(queued2.getId(), is(taskAttempt2.getId()));

    }

    @Test
    public void takeFromQueueNoCapacityShouldReturnNull() {
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun()
                .cloneBuilder().withQueueName("test").build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun()
                .cloneBuilder().withQueueName("test").build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt(taskRun2);
        taskRunDao.createAttempt(taskAttempt1);
        taskRunDao.createAttempt(taskAttempt2);
        spyManager.submit(taskAttempt1);
        spyManager.submit(taskAttempt2);
        TaskAttempt queued1 = spyManager.take();
        assertNull(queued1);
    }

    @Test
    public void testChangeAttemptPriority() {
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
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createAttempt(taskAttempt1);
        taskRunDao.createAttempt(taskAttempt2);
        taskRunDao.createAttempt(taskAttempt3);
        spyManager.submit(taskAttempt1);
        spyManager.submit(taskAttempt2);
        spyManager.submit(taskAttempt3);
        spyManager.changePriority(taskAttempt3.getId(), taskAttempt3.getQueueName(), TaskPriority.HIGHEST);
        assertThat(spyManager.take().getId(), is(taskAttempt3.getId()));
        assertThat(spyManager.take().getId(), is(taskAttempt2.getId()));
        assertThat(spyManager.take().getId(), is(taskAttempt1.getId()));

    }


}
