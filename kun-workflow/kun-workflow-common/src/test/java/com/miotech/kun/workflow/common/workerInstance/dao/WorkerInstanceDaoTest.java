package com.miotech.kun.workflow.common.workerInstance.dao;

import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.workerInstance.WorkerInstanceDao;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceEnv;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class WorkerInstanceDaoTest extends CommonTestBase {

    @Inject
    private WorkerInstanceDao workerInstanceDao;

    @Test
    public void createWorkerInstance() {
        long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        WorkerInstance instance = WorkerInstance
                .newBuilder()
                .withTaskAttemptId(taskAttemptId)
                .withWorkerId("kubernetes_" + taskAttemptId)
                .withEnv(WorkerInstanceEnv.KUBERNETES)
                .build();
        workerInstanceDao.createWorkerInstance(instance);
        WorkerInstance savedInstance = workerInstanceDao.getWorkerInstanceByAttempt(taskAttemptId);
        assertThat(savedInstance.getTaskAttemptId(), is(instance.getTaskAttemptId()));
        assertThat(savedInstance.getWorkerId(), is(instance.getWorkerId()));
        assertThat(savedInstance.getEnv(), is(instance.getEnv()));
    }

    @Test
    public void updateWorkerInstance() {
        long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        WorkerInstance instance = WorkerInstance
                .newBuilder()
                .withTaskAttemptId(taskAttemptId)
                .withWorkerId("old_worker_" + taskAttemptId)
                .withEnv(WorkerInstanceEnv.KUBERNETES)
                .build();
        workerInstanceDao.createWorkerInstance(instance);
        WorkerInstance newInstance = instance.cloneBuilder()
                .withWorkerId("new_worker")
                .build();
        workerInstanceDao.updateWorkerInstance(newInstance);
        WorkerInstance savedInstance = workerInstanceDao.getWorkerInstanceByAttempt(taskAttemptId);
        assertThat(savedInstance.getTaskAttemptId(), is(newInstance.getTaskAttemptId()));
        assertThat(savedInstance.getWorkerId(), is(newInstance.getWorkerId()));
        assertThat(savedInstance.getEnv(), is(newInstance.getEnv()));

    }

    @Test
    public void getActiveWorkerInstance() {
        long taskRunId1 = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId1 = WorkflowIdGenerator.nextTaskAttemptId(taskRunId1, 1);
        WorkerInstance instance1 = WorkerInstance
                .newBuilder()
                .withTaskAttemptId(taskAttemptId1)
                .withWorkerId("worker1_" + taskAttemptId1)
                .withEnv(WorkerInstanceEnv.KUBERNETES)
                .build();
        workerInstanceDao.createWorkerInstance(instance1);
        long taskRunId2 = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId2 = WorkflowIdGenerator.nextTaskAttemptId(taskRunId2, 1);
        WorkerInstance instance2 = WorkerInstance
                .newBuilder()
                .withTaskAttemptId(taskAttemptId2)
                .withWorkerId("worker2_" + taskAttemptId2)
                .withEnv(WorkerInstanceEnv.KUBERNETES)
                .build();
        workerInstanceDao.createWorkerInstance(instance2);
        List<WorkerInstance> instanceList = workerInstanceDao.getActiveWorkerInstance(WorkerInstanceEnv.KUBERNETES);
        assertThat(instanceList.size(), is(2));
        assertThat(instanceList, containsInAnyOrder(instance1, instance2));

    }

    @Test
    public void deleteWorkerInstance() {
        long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        WorkerInstance instance = WorkerInstance
                .newBuilder()
                .withTaskAttemptId(taskAttemptId)
                .withWorkerId("worker1_" + taskAttemptId)
                .withEnv(WorkerInstanceEnv.KUBERNETES)
                .build();
        workerInstanceDao.createWorkerInstance(instance);
        workerInstanceDao.deleteWorkerInstance(taskAttemptId,WorkerInstanceEnv.KUBERNETES);
        List<WorkerInstance> instanceList = workerInstanceDao.getActiveWorkerInstance(WorkerInstanceEnv.KUBERNETES);
        assertThat(instanceList.size(), is(0));

    }

    @Test
    public void insert_taskAttempt_with_diff_worker_id_should_return_existOne() {
        long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        WorkerInstance instance1 = WorkerInstance
                .newBuilder()
                .withNameSpace("default")
                .withTaskAttemptId(taskAttemptId)
                .withWorkerId("first_" + taskAttemptId)
                .withEnv(WorkerInstanceEnv.KUBERNETES)
                .build();
        workerInstanceDao.createWorkerInstance(instance1);
        WorkerInstance instance2 = WorkerInstance
                .newBuilder()
                .withNameSpace("default")
                .withTaskAttemptId(taskAttemptId)
                .withWorkerId("second_" + taskAttemptId)
                .withEnv(WorkerInstanceEnv.KUBERNETES)
                .build();
        WorkerInstance exist =workerInstanceDao.createWorkerInstance(instance2);
        assertThat(exist,is(instance1));
    }

}
