package com.miotech.kun.workflow.worker;

import com.miotech.kun.workflow.core.execution.ExecCommand;

public interface Worker {

    public void killTask(Boolean abort);

    public void start(ExecCommand command);

    public boolean shutdown();

}
