package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.common.WorkerInstance;
import com.miotech.kun.workflow.core.model.common.WorkerSnapshot;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.executor.CommonTestBase;
import com.miotech.kun.workflow.executor.EventHandler;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class EventHandlerTest extends CommonTestBase {

    private PodEventMonitor podEventMonitor;

    private KubernetesClient client;

    @Inject
    private TaskDao taskDao;
    @Inject
    private TaskRunDao taskRunDao;

    @Before
    public void init(){
        client = mock(KubernetesClient.class);
    }

    @Test
    public void pollingEventTest(){
        //prepare
        TaskAttempt taskAttempt = prepareTaskAttempt();
        Pod createdPod = MockPodFactory.create(taskAttempt.getId(),"Pending");
        PodList podList = new PodList();
        List<Pod> items = new ArrayList<>();
        items.add(createdPod);
        podList.setItems(items);
        MixedOperation mockMixedOperation = mock(MixedOperation.class);
        doReturn(mockMixedOperation).when(client).pods();
        FilterWatchListDeletable mockFilter = mock(FilterWatchListDeletable.class);
        doReturn(mockFilter).when(mockMixedOperation).withLabel(any());
        doReturn(podList).when(mockFilter).list();
        podEventMonitor = new PodEventMonitor(client);
        podEventMonitor.start();
        WorkerInstance instance = new WorkerInstance(taskAttempt.getId(),"kubernetes-"+taskAttempt.getId());
        EventHandler testHandler = new EventHandler() {
            @Override
            public void onReceiveSnapshot(WorkerSnapshot workerSnapshot) {
            }

            @Override
            public void onReceivePollingSnapShot(WorkerSnapshot workerSnapshot) {
                taskRunDao.updateTaskAttemptStatus(workerSnapshot.getIns().getTaskAttemptId(),workerSnapshot.getStatus());
            }
        };
        podEventMonitor.register(instance,testHandler);
        awaitUntilAttemptInit(taskAttempt.getId());
    }

    private TaskAttempt prepareTaskAttempt(){
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        taskDao.create(taskAttempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(taskAttempt.getTaskRun());
        taskRunDao.createAttempt(taskAttempt);
        return taskAttempt;
    }

    private void awaitUntilAttemptInit(long attemptId) {
        await().atMost(20, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(attemptId);
            return s.isPresent() && (s.get().equals(TaskRunStatus.INITIALIZING));
        });
    }

}

