package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.model.common.WorkerInstance;
import com.miotech.kun.workflow.core.model.common.WorkerSnapshot;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

import java.util.List;

public interface LifeCycleManage {
    public WorkerInstance start(TaskAttempt taskAttempt);
    public WorkerInstance stop(TaskAttempt taskAttempt);
    public WorkerSnapshot get(TaskAttempt taskAttempt);
    public List<WorkerInstance> getRunningWorker();
}
