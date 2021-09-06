package com.miotech.kun.workflow.executor;


import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.publish.NopEventPublisher;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockQueueManager;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockWorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockWorkerMonitor;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WorkerLifeCycleManagerTest extends CommonTestBase{

    @Inject
    private MockWorkerLifeCycleManager workerLifeCycleManager;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    private Props props;

    @Inject
    private MockQueueManager mockQueueManager;

    private MockWorkerMonitor workerMonitor = new MockWorkerMonitor();

    @Inject
    private MockWorkerLifeCycleManager mockWorkerLifeCycleManager;

    @Override
    protected void configuration() {

        props = new Props();
        props.put("executor.env.resourceQueues", "default,test");
        props.put("executor.env.resourceQueues.default.quota.workerNumbers", 2);
        props.put("executor.env.resourceQueues.test.quota.workerNumbers", 2);
        super.configuration();
        bind(MetadataServiceFacade.class,mock(MetadataServiceFacade.class));
        bind(Props.class, props);
        bind(EventBus.class, new EventBus());
        bind(KubernetesClient.class, mock(KubernetesClient.class));
        bind(EventPublisher.class, new NopEventPublisher());
        bind(WorkerLifeCycleManager.class, MockWorkerLifeCycleManager.class);
        bind(AbstractQueueManager.class, MockQueueManager.class);
        bind(WorkerMonitor.class, workerMonitor);
    }

    @Before
    public void init(){
        mockWorkerLifeCycleManager = spy(workerLifeCycleManager);
    }

    @Test
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
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        //first retry
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        //second retry
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        awaitUntilAttemptDone(taskAttempt.getId());

        TaskAttempt savedAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(savedAttempt.getAttempt(), is(1));
        assertThat(savedAttempt.getStatus(), is(TaskRunStatus.FAILED));
        assertThat(savedAttempt.getRetryTimes(),is(2));

    }

    @Test
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
        mockWorkerLifeCycleManager.markFailed(taskAttempt.getId());

        //first retry
        awaitUntilAttemptStarted(taskAttempt.getId());
        mockWorkerLifeCycleManager.markDone(taskAttempt.getId());

        awaitUntilAttemptDone(taskAttempt.getId());

        TaskAttempt savedAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(savedAttempt.getAttempt(), is(1));
        assertThat(savedAttempt.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(savedAttempt.getRetryTimes(),is(1));

    }

    @Test
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
