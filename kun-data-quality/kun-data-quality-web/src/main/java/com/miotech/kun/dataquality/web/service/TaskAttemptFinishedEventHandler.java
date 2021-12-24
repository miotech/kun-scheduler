package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;

public interface TaskAttemptFinishedEventHandler {

    void handle(TaskAttemptFinishedEvent event);

}
