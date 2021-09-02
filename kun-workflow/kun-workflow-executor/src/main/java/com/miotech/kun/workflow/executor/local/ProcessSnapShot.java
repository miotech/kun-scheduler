package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceKind;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.utils.DateTimeUtils;

public class ProcessSnapShot extends WorkerSnapshot {

    private final TaskRunStatus taskRunStatus;

    public ProcessSnapShot(WorkerInstance ins, TaskRunStatus taskRunStatus) {
        super(ins, DateTimeUtils.now());
        this.taskRunStatus = taskRunStatus;
    }

    @Override
    public TaskRunStatus getStatus() {
        return taskRunStatus;
    }

    public static ProcessSnapShot fromTaskAttemptMessage(TaskAttemptMsg taskAttemptMsg){
        WorkerInstance workerInstance = new WorkerInstance(taskAttemptMsg.getTaskAttemptId(),
                String.valueOf(taskAttemptMsg.getWorkerId()),"local", WorkerInstanceKind.LOCAL_PROCESS);
        return new ProcessSnapShot(workerInstance,taskAttemptMsg.getTaskRunStatus());
    }
}
