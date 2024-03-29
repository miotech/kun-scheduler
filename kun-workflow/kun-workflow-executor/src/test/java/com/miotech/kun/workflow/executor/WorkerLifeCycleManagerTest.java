package com.miotech.kun.workflow.executor;


import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.TaskRunStateMachineDispatcher;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockQueueManager;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockWorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockWorkerMonitor;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

//弃用，完全基于mock实现的testcase，重构后差异过大
public class WorkerLifeCycleManagerTest extends CommonTestBase{

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    private MockQueueManager mockQueueManager;

    private MockWorkerMonitor workerMonitor = new MockWorkerMonitor();

    private MockWorkerLifeCycleManager mockWorkerLifeCycleManager;

    @Inject
    private TaskRunStateMachineDispatcher taskRunStateMachineDispatcher;

    @Inject
    private Injector injector;

    @Override
    protected void configuration() {

        super.configuration();
        bind(MetadataServiceFacade.class,mock(MetadataServiceFacade.class));
        bind(LineageServiceFacade.class,mock(LineageServiceFacade.class));
        bind(EventBus.class, new EventBus());
        bind(KubernetesClient.class, mock(KubernetesClient.class));
        bind(EventPublisher.class, new NopEventPublisher());
        bind(EventSubscriber.class, mock(EventSubscriber.class));
    }

    @BeforeEach
    public void init(){
        ExecutorConfig executorConfig = new ExecutorConfig();
        ResourceQueue defaultQueue = ResourceQueue.newBuilder()
                .withQueueName("default")
                .withWorkerNumbers(2)
                .build();
        ResourceQueue testQueue = ResourceQueue.newBuilder()
                .withQueueName("test")
                .withWorkerNumbers(2)
                .build();
        executorConfig.setResourceQueues(Lists.newArrayList(defaultQueue,testQueue));
        mockQueueManager = new MockQueueManager(executorConfig);
        mockQueueManager.injectMember(injector);
        mockWorkerLifeCycleManager = new MockWorkerLifeCycleManager(executorConfig, workerMonitor, mockQueueManager);
        mockWorkerLifeCycleManager.injectMembers(injector);
        mockWorkerLifeCycleManager.init();
        taskRunStateMachineDispatcher.start();
    }

    @AfterEach
    public void teardown(){
        mockWorkerLifeCycleManager.shutdown();
    }

    @Disabled
    public void testTaskAttemptFailed_should_retry_to_limit() {
        Task task1 = MockTaskFactory.createTaskWithRetry(2, 1);
        taskDao.create(task1);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task1);
        taskRunDao.createTaskRun(taskRun);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskRunDao.createAttempt(taskAttempt);

        mockWorkerLifeCycleManager.start(taskAttempt);
        //execute
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markRunning(taskAttempt.getId());

        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        //first retry
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markRunning(taskAttempt.getId());
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        //second retry
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markRunning(taskAttempt.getId());
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        awaitUntilAttemptDone(taskAttempt.getId());

        TaskAttempt savedAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(savedAttempt.getAttempt(), is(1));
        assertThat(savedAttempt.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(savedAttempt.getRetryTimes(),is(2));

    }

    @Disabled
    public void testTaskAttemptFailed_should_retry_to_success() {
        Task task1 = MockTaskFactory.createTaskWithRetry(2, 1);
        taskDao.create(task1);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task1);
        taskRunDao.createTaskRun(taskRun);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskRunDao.createAttempt(taskAttempt);

        mockWorkerLifeCycleManager.start(taskAttempt);

        //execute
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markRunning(taskAttempt.getId());
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        //first retry
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markRunning(taskAttempt.getId());
        mockWorkerLifeCycleManager.markDone(taskAttempt.getId());

        awaitUntilAttemptDone(taskAttempt.getId());

        TaskAttempt savedAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(savedAttempt.getAttempt(), is(1));
        assertThat(savedAttempt.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(savedAttempt.getRetryTimes(),is(1));

    }

    @Disabled
    public void testTaskWithRetriesZero_should_not_retry() {
        Task task1 = MockTaskFactory.createTaskWithRetry(0, 1);
        taskDao.create(task1);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task1);
        taskRunDao.createTaskRun(taskRun);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskRunDao.createAttempt(taskAttempt);

        mockWorkerLifeCycleManager.start(taskAttempt);

        //execute
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markRunning(taskAttempt.getId());
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        awaitUntilAttemptDone(taskAttempt.getId());

        TaskAttempt savedAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(savedAttempt.getAttempt(), is(1));
        assertThat(savedAttempt.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(savedAttempt.getRetryTimes(),is(0));

    }


    private void awaitUntilAttemptStarted(long attemptId) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            return mockWorkerLifeCycleManager.hasRegister(attemptId);
        });
    }


    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
        });
    }
}
