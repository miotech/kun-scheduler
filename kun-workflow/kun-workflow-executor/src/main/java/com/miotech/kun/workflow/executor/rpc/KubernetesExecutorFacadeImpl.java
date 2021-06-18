package com.miotech.kun.workflow.executor.rpc;

import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;

public class KubernetesExecutorFacadeImpl implements WorkflowExecutorFacade {
    @Override
    public boolean statusUpdate(TaskAttemptMsg attemptMsg) {
        return false;
    }

    @Override
    public boolean heartBeat(HeartBeatMessage heartBeatMessage) {
        return false;
    }
}
