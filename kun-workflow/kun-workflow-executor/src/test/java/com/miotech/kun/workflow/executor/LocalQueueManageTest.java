package com.miotech.kun.workflow.executor;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.executor.local.LocalProcessBackend;
import com.miotech.kun.workflow.executor.local.LocalQueueManage;
import com.miotech.kun.workflow.executor.local.PublicEventHandler;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory.createTaskAttempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LocalQueueManageTest extends CommonTestBase {

    private final Logger logger = LoggerFactory.getLogger(LocalQueueManageTest.class);

    @Inject
    private PublicEventHandler publicEventHandler;

    @Inject
    private LocalProcessBackend localProcessBackend;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private EventBus eventBus;

    @Inject
    private Injector injector;

    @Override
    protected void configuration() {
        super.configuration();
        bind(MetadataServiceFacade.class,mock(MetadataServiceFacade.class));
        bind(LineageServiceFacade.class,mock(LineageServiceFacade.class));
        bind(EventPublisher.class, new NopEventPublisher());
        bind(EventSubscriber.class, mock(EventSubscriber.class));
    }

    @Test
    public void testChangeAttemptPriority() {
        //prepare
        ExecutorConfig executorConfig = new ExecutorConfig();
        ResourceQueue defaultQueue = ResourceQueue.newBuilder()
                .withQueueName("default")
                .withWorkerNumbers(3)
                .build();
        ResourceQueue testQueue = ResourceQueue.newBuilder()
                .withQueueName("test")
                .withWorkerNumbers(0)
                .build();
        executorConfig.setResourceQueues(Lists.newArrayList(defaultQueue,testQueue));

        LocalQueueManage localQueueManage = prepareQueueManage(executorConfig);
        Task task1 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(16).build();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        Task task2 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(24).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        Task task3 = MockTaskFactory.createTask().cloneBuilder().
                withPriority(8).build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        TaskAttempt taskAttempt3 = createTaskAttempt(taskRun3);

        saveTaskAttempt(taskAttempt1);
        saveTaskAttempt(taskAttempt2);
        saveTaskAttempt(taskAttempt3);

        localQueueManage.submit(taskAttempt1);
        localQueueManage.submit(taskAttempt2);
        localQueueManage.submit(taskAttempt3);

        localQueueManage.changePriority(taskAttempt3.getId(),"default", 32);
        List<TaskAttempt> queuedAttempts = localQueueManage.drain();

        assertThat(queuedAttempts.get(0).getId(), is(taskAttempt3.getId()));
        assertThat(queuedAttempts.get(1).getId(), is(taskAttempt2.getId()));
        assertThat(queuedAttempts.get(2).getId(), is(taskAttempt1.getId()));

    }

    @Test
    public void testSamePriorityShouldFIFO_EnqueueWithSameCreateOrder() throws InterruptedException {
        //prepare
        ExecutorConfig executorConfig = new ExecutorConfig();
        ResourceQueue defaultQueue = ResourceQueue.newBuilder()
                .withQueueName("default")
                .withWorkerNumbers(3)
                .build();
        ResourceQueue testQueue = ResourceQueue.newBuilder()
                .withQueueName("test")
                .withWorkerNumbers(0)
                .build();
        executorConfig.setResourceQueues(Lists.newArrayList(defaultQueue,testQueue));

        LocalQueueManage localQueueManage = prepareQueueManage(executorConfig);
        Task task1 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        Task task2 = MockTaskFactory.createTask();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        Task task3 = MockTaskFactory.createTask();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        TaskAttempt taskAttempt3 = createTaskAttempt(taskRun3);

        saveTaskAttempt(taskAttempt1);
        saveTaskAttempt(taskAttempt2);
        saveTaskAttempt(taskAttempt3);

        localQueueManage.submit(taskAttempt1);
        Thread.sleep(1000);
        localQueueManage.submit(taskAttempt2);
        Thread.sleep(1000);
        localQueueManage.submit(taskAttempt3);

        //verify
        List<TaskAttempt> queuedAttempts = localQueueManage.drain();
        assertThat(queuedAttempts.get(0).getId(), is(taskAttempt1.getId()));
        assertThat(queuedAttempts.get(1).getId(), is(taskAttempt2.getId()));
        assertThat(queuedAttempts.get(2).getId(), is(taskAttempt3.getId()));

    }

    @Test
    public void testSamePriorityShouldFIFO_EnqueueWithDifferentCreateOrder() throws InterruptedException {
        //prepare
        ExecutorConfig executorConfig = new ExecutorConfig();
        ResourceQueue defaultQueue = ResourceQueue.newBuilder()
                .withQueueName("default")
                .withWorkerNumbers(3)
                .build();
        ResourceQueue testQueue = ResourceQueue.newBuilder()
                .withQueueName("test")
                .withWorkerNumbers(0)
                .build();
        executorConfig.setResourceQueues(Lists.newArrayList(defaultQueue,testQueue));

        LocalQueueManage localQueueManage = prepareQueueManage(executorConfig);
        Task task1 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        Task task2 = MockTaskFactory.createTask();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        Task task3 = MockTaskFactory.createTask();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        TaskAttempt taskAttempt3 = createTaskAttempt(taskRun3);

        saveTaskAttempt(taskAttempt1);
        saveTaskAttempt(taskAttempt2);
        saveTaskAttempt(taskAttempt3);

        localQueueManage.submit(taskAttempt3);
        Thread.sleep(1000);
        localQueueManage.submit(taskAttempt2);
        Thread.sleep(1000);
        localQueueManage.submit(taskAttempt1);

        //verify
        List<TaskAttempt> queuedAttempts = localQueueManage.drain();
        assertThat(queuedAttempts.get(0).getId(), is(taskAttempt3.getId()));
        assertThat(queuedAttempts.get(1).getId(), is(taskAttempt2.getId()));
        assertThat(queuedAttempts.get(2).getId(), is(taskAttempt1.getId()));
    }

    @Test
    public void testSamePriorityShouldFIFO_SameEnqueueTimeWithDifferentCreateOrder() throws InterruptedException {
        //prepare
        ExecutorConfig executorConfig = new ExecutorConfig();
        ResourceQueue defaultQueue = ResourceQueue.newBuilder()
                .withQueueName("default")
                .withWorkerNumbers(3)
                .build();
        ResourceQueue testQueue = ResourceQueue.newBuilder()
                .withQueueName("test")
                .withWorkerNumbers(0)
                .build();
        executorConfig.setResourceQueues(Lists.newArrayList(defaultQueue,testQueue));

        LocalQueueManage localQueueManage = prepareQueueManage(executorConfig);
        Task task1 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = createTaskAttempt(taskRun1);
        Task task2 = MockTaskFactory.createTask();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskAttempt taskAttempt2 = createTaskAttempt(taskRun2);
        Task task3 = MockTaskFactory.createTask();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        TaskAttempt taskAttempt3 = createTaskAttempt(taskRun3);

        saveTaskAttempt(taskAttempt1);
        saveTaskAttempt(taskAttempt2);
        saveTaskAttempt(taskAttempt3);

        localQueueManage.submit(taskAttempt3);
        Thread.sleep(1000);
        DateTimeUtils.freeze();
        localQueueManage.submit(taskAttempt2);
        localQueueManage.submit(taskAttempt1);
        DateTimeUtils.resetClock();

        //verify
        List<TaskAttempt> queuedAttempts = localQueueManage.drain();
        assertThat(queuedAttempts.get(0).getId(), is(taskAttempt3.getId()));
        assertThat(queuedAttempts.get(1).getId(), is(taskAttempt1.getId()));
        assertThat(queuedAttempts.get(2).getId(), is(taskAttempt2.getId()));

    }

    private LocalQueueManage prepareQueueManage(ExecutorConfig executorConfig) {
        LocalQueueManage localQueueManage = new LocalQueueManage(executorConfig,localProcessBackend);
        localQueueManage.injectMember(injector);
        localQueueManage.init();
        return localQueueManage;
    }

    private void saveTaskAttempt(TaskAttempt taskAttempt){
        taskDao.create(taskAttempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(taskAttempt.getTaskRun());
        taskRunDao.createAttempt(taskAttempt);
    }


}
