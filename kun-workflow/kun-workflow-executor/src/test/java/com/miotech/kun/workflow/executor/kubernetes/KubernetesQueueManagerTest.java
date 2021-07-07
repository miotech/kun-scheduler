package com.miotech.kun.workflow.executor.kubernetes;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.publish.NopEventPublisher;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.CommonTestBase;
import com.miotech.kun.workflow.executor.TaskAttemptQueue;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockQueueManager;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockWorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.kubernetes.mock.MockWorkerMonitor;
import com.miotech.kun.workflow.executor.local.MiscService;
import com.miotech.kun.workflow.testing.event.EventCollector;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doAnswer;

public class KubernetesQueueManagerTest extends CommonTestBase {

    private KubernetesResourceManager kubernetesResourceManager;

    private KubernetesResourceManager spyManager;

    private MockWorkerMonitor workerMonitor = new MockWorkerMonitor();

    @Inject
    private KubernetesExecutor executor;

    @Inject
    private MiscService miscService;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    private Props mockProps;

    @Inject
    private MockWorkerLifeCycleManager workerLifeCycleManager;

    @Inject
    private MockQueueManager mockQueueManager;

    @Inject
    private EventBus eventBus;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private EventCollector eventCollector;

    @Override
    protected void configuration() {
        mockProps = new Props();
        mockProps.put("executor.env.resourceQueues", "default,test");
        mockProps.put("executor.env.resourceQueues.default.quota.workerNumbers", 2);
        mockProps.put("executor.env.resourceQueues.test.quota.workerNumbers", 2);
        super.configuration();
        MetadataServiceFacade mockMetadataServiceFacade= mock(MetadataServiceFacade.class);
        bind(MetadataServiceFacade.class,mockMetadataServiceFacade);
        bind(Props.class, mockProps);
        bind(EventBus.class, new EventBus());
        bind(KubernetesClient.class, mock(KubernetesClient.class));
        bind(EventPublisher.class, new NopEventPublisher());
        bind(WorkerLifeCycleManager.class, MockWorkerLifeCycleManager.class);
        bind(AbstractQueueManager.class, MockQueueManager.class);
        bind(WorkerMonitor.class, workerMonitor);
    }

    @Test
    public void testSubmit_under_limit_run_immediately() {
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt();
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt();
        saveAttempt(taskAttempt1);
        saveAttempt(taskAttempt2);
        executor.submit(taskAttempt1);
        executor.submit(taskAttempt2);

        //wait attempt start
        awaitUntilAttemptStarted(taskAttempt1.getId());
        awaitUntilAttemptStarted(taskAttempt2.getId());

        //worker run finish
        workerLifeCycleManager.markDone(taskAttempt1.getId());
        workerLifeCycleManager.markDone(taskAttempt2.getId());

        awaitUntilAttemptDone(taskAttempt1.getId());
        awaitUntilAttemptDone(taskAttempt2.getId());

        //verify
        TaskAttempt savedAttempt1 = taskRunDao.fetchAttemptById(taskAttempt1.getId()).get();
        TaskAttempt savedAttempt2 = taskRunDao.fetchAttemptById(taskAttempt2.getId()).get();

        assertThat(savedAttempt1.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(savedAttempt2.getStatus(), is(TaskRunStatus.SUCCESS));


    }

    @Test
    public void testSubmit_over_limit() {
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt();
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt();
        TaskAttempt taskAttempt3 = MockTaskAttemptFactory.createTaskAttempt();
        saveAttempt(taskAttempt1);
        saveAttempt(taskAttempt2);
        saveAttempt(taskAttempt3);
        executor.submit(taskAttempt1);
        executor.submit(taskAttempt2);
        executor.submit(taskAttempt3);

        //wait attempt1,2 start
        awaitUntilAttemptStarted(taskAttempt1.getId());
        awaitUntilAttemptStarted(taskAttempt2.getId());

        assertTaskQueueing(taskAttempt3);

        assertThat(workerLifeCycleManager.getRunningWorker().size(), is(2));
        workerLifeCycleManager.markDone(taskAttempt1.getId());

        //wait attempt3 start
        awaitUntilAttemptStarted(taskAttempt3.getId());
        workerLifeCycleManager.markDone(taskAttempt3.getId());
        //verify
        TaskAttempt savedAttempt3 = taskRunDao.fetchAttemptById(taskAttempt3.getId()).get();
        assertThat(savedAttempt3.getStatus(), is(TaskRunStatus.SUCCESS));

    }

    private void assertTaskQueueing(TaskAttempt taskAttempt) {
        // await until task is queued
        awaitUntilAttemptQueued(taskAttempt.getId());
        // last for 1 second
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        //still in queue
        TaskAttempt saved = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(saved.getStatus(), is(TaskRunStatus.QUEUED));
    }

    @Test
    public void testSubmit_multiple_queues() {
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt();
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttemptWithQueueName("test");
        saveAttempt(taskAttempt1);
        saveAttempt(taskAttempt2);
        executor.submit(taskAttempt1);
        executor.submit(taskAttempt2);

        //wait attempt start
        awaitUntilAttemptStarted(taskAttempt1.getId());
        awaitUntilAttemptStarted(taskAttempt2.getId());

        //worker run finish
        workerLifeCycleManager.markDone(taskAttempt1.getId());
        workerLifeCycleManager.markDone(taskAttempt2.getId());

        awaitUntilAttemptDone(taskAttempt1.getId());
        awaitUntilAttemptDone(taskAttempt2.getId());

        //verify
        TaskAttempt savedAttempt1 = taskRunDao.fetchAttemptById(taskAttempt1.getId()).get();
        TaskAttempt savedAttempt2 = taskRunDao.fetchAttemptById(taskAttempt2.getId()).get();

        assertThat(savedAttempt1.getStatus(), is(TaskRunStatus.SUCCESS));
        assertThat(savedAttempt2.getStatus(), is(TaskRunStatus.SUCCESS));
    }

    @Test
    public void testSubmit_multiple_threads_submit_to_same_queue() {
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt();
                    TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt();
                    TaskAttempt taskAttempt3 = MockTaskAttemptFactory.createTaskAttempt();
                    saveAttempt(taskAttempt1);
                    saveAttempt(taskAttempt2);
                    saveAttempt(taskAttempt3);
                    executor.submit(taskAttempt1);
                    executor.submit(taskAttempt2);
                    executor.submit(taskAttempt3);
                }
            });
            thread.start();
        }

        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);

        assertThat(workerLifeCycleManager.getRunningWorker().size(), is(2));

        assertThat(mockQueueManager.getQueuedNum("default"), is(7));

    }

    @Test
    public void abortTaskAttemptInQUEUE(){
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt();
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt();
        TaskAttempt taskAttempt3 = MockTaskAttemptFactory.createTaskAttempt();
        saveAttempt(taskAttempt1);
        saveAttempt(taskAttempt2);
        saveAttempt(taskAttempt3);
        executor.submit(taskAttempt1);
        executor.submit(taskAttempt2);
        executor.submit(taskAttempt3);

        awaitUntilAttemptQueued(taskAttempt3.getId());

        //abort taskAttempt
        executor.cancel(taskAttempt3.getId());

        awaitUntilAttemptDone(taskAttempt3.getId());

        // verify
        TaskAttempt savedAttempt3 = taskRunDao.fetchAttemptById(taskAttempt3.getId()).get();
        assertThat(savedAttempt3.getAttempt(), is(1));
        assertThat(savedAttempt3.getStatus(), is(TaskRunStatus.ABORTED));

        // events
        assertStatusHistory(savedAttempt3.getId(),
                TaskRunStatus.CREATED,
                TaskRunStatus.QUEUED,
                TaskRunStatus.ABORTED);

    }

    @Before
    public void init() {
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
        kubernetesResourceManager = prepareQueueManage();
        spyManager = spy(kubernetesResourceManager);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                TaskAttemptQueue taskAttemptQueue = invocation.getArgument(0, TaskAttemptQueue.class);
                if (taskAttemptQueue.getName().equals("default")) {
                    return 1;
                }
                return 0;
            }
        }).when(spyManager).getCapacity(ArgumentMatchers.any());
    }

    private KubernetesResourceManager prepareQueueManage() {
        KubernetesResourceManager queueManager = new KubernetesResourceManager(mock(KubernetesClient.class), mockProps, miscService);
        queueManager.init();
        return queueManager;
    }


    private void saveAttempt(TaskAttempt taskAttempt) {
        taskDao.create(taskAttempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(taskAttempt.getTaskRun());
        taskRunDao.createAttempt(taskAttempt);
    }


    private void awaitUntilAttemptDone(long attemptId) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().isFinished());
        });
    }

    private void awaitUntilAttemptQueued(long attemptId) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().equals(TaskRunStatus.QUEUED));
        });
    }

    private void awaitUntilAttemptStarted(long attemptId) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            return workerLifeCycleManager.hasRegister(attemptId);
        });
    }

    private void assertStatusHistory(Long attemptId, TaskRunStatus... asserts) {
        checkArgument(asserts.length > 1);

        List<Event> events = eventCollector.getEvents();

        List<Event> eventsOfAttempt = events.stream()
                .filter(e -> e instanceof TaskAttemptStatusChangeEvent &&
                        ((TaskAttemptStatusChangeEvent) e).getAttemptId() == attemptId)
                .collect(Collectors.toList());

        for (int i = 0; i < asserts.length - 1; i++) {
            TaskAttemptStatusChangeEvent event = (TaskAttemptStatusChangeEvent) eventsOfAttempt.get(i);
            assertThat(event.getAttemptId(), is(attemptId));
            assertThat(event.getFromStatus(), is(asserts[i]));
            assertThat(event.getToStatus(), is(asserts[i + 1]));
        }
    }


}