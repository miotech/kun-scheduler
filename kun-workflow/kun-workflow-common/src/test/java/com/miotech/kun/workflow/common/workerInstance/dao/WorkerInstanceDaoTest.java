package com.miotech.kun.workflow.common.workerInstance.dao;

import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.workerInstance.WorkerInstanceDao;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.Test;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
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

    }

    @Test
    public void deleteWorkerInstance() {

    }

    @Test
    public void insert_taskAttempt_with_diff_worker_id_should_throws_exception() {

    }

    @Test
    public void create_taskAttempt_with_diff_worker_id_should_throws_exception() {

    }
}
