package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceKind;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.CommonTestBase;
import com.miotech.kun.workflow.executor.WorkerEventHandler;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@Disabled
public class WorkerEventHandlerTest extends CommonTestBase {

    private PodEventMonitor podEventMonitor;

    private KubernetesClient client;

    @Inject
    private TaskDao taskDao;
    @Inject
    private TaskRunDao taskRunDao;
    private Props props;

    @BeforeEach
    public void init() {
        props = new Props();
        props.put("executor.env.namespace","default");
        client = mock(KubernetesClient.class);
    }

    @Test
    public void pollingEventTest() {
        //prepare
        TaskAttempt taskAttempt = prepareTaskAttempt();
        Pod createdPod = MockPodFactory.create(taskAttempt.getId(), "Pending");
        PodList podList = new PodList();
        List<Pod> items = new ArrayList<>();
        items.add(createdPod);
        podList.setItems(items);
        MixedOperation mockMixedOperation = mock(MixedOperation.class);
        doReturn(mockMixedOperation).when(client).pods();
        FilterWatchListDeletable mockFilter = mock(FilterWatchListDeletable.class);
        doReturn(mockFilter).when(mockMixedOperation).withLabel(any());
        doReturn(mockFilter).when(mockMixedOperation).withLabel(any(),any());
        doReturn(mockFilter).when(mockFilter).withLabel(any());
        doReturn(mockFilter).when(mockFilter).withLabel(any(),any());
        doReturn(podList).when(mockFilter).list();
        doReturn(null).when(mockFilter).watch(any());
        podEventMonitor = new PodEventMonitor(client, props, "test");
        podEventMonitor.start();
        WorkerInstance instance = new WorkerInstance(taskAttempt.getId(),
                "kubernetes-" + taskAttempt.getId(), taskAttempt.getQueueName(), WorkerInstanceKind.KUBERNETES);
        WorkerEventHandler testHandler = new WorkerEventHandler() {
            @Override
            public void onReceiveSnapshot(WorkerSnapshot workerSnapshot) {
            }

            @Override
            public void onReceivePollingSnapShot(WorkerSnapshot workerSnapshot) {
                taskRunDao.updateTaskAttemptStatus(workerSnapshot.getIns().getTaskAttemptId(), workerSnapshot.getStatus());
            }
        };
        podEventMonitor.register(instance.getTaskAttemptId(), testHandler);
        TaskAttemptProps taskAttemptProps = taskRunDao.fetchLatestTaskAttempt(taskAttempt.getTaskRun().getId());
        assertThat(taskAttemptProps.getId(),is(taskAttempt.getId()));
    }

    private TaskAttempt prepareTaskAttempt() {
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        taskDao.create(taskAttempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(taskAttempt.getTaskRun());
        taskRunDao.createAttempt(taskAttempt);
        return taskAttempt;
    }

}

