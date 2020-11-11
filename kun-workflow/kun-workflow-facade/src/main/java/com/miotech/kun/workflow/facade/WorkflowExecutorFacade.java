package com.miotech.kun.workflow.facade;

import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;

public interface WorkflowExecutorFacade {
    public boolean statusUpdate(TaskAttemptMsg attemptMsg);

    public boolean heartBeat(HeartBeatMessage heartBeatMessage);

}
