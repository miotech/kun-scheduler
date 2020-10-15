package com.miotech.kun.workflow.worker;

import com.miotech.kun.workflow.core.execution.ExecCommand;

public interface Worker {

    public void killTask();

    public void start(ExecCommand command);

}
