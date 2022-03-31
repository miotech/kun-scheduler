package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;

public interface TaskAttemptStatusChangeEventHandler {

    void handle(TaskAttemptStatusChangeEvent event);

}
