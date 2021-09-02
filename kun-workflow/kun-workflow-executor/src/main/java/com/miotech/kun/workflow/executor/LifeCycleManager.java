package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

import java.util.List;

public interface LifeCycleManager {
    /**
     * start to run taskAttempt
     * @param taskAttempt
     */
    public void start(TaskAttempt taskAttempt);
    /**
     * stop taskAttempt witch is not finished yet
     * @param taskAttemptId
     */
    public void stop(Long taskAttemptId);

    /**
     * get a workerSnapshot with taskAttemptId
     * @param taskAttemptId
     * @return
     */
    public WorkerSnapshot get(Long taskAttemptId);

    /**
     * get all running worker
     * @return
     */
    public List<WorkerInstance> getRunningWorker();

    /**
     * get taskAttempt logs from worker
     * @param taskAttemptId
     * @param tailLines
     * @return
     */
    public String getLog(Long taskAttemptId, Integer tailLines);
}
