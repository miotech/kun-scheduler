package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class MockTaskAttemptStatusChangeEventFactory {

    private MockTaskAttemptStatusChangeEventFactory() {
    }

    public static TaskAttemptStatusChangeEvent create(TaskRunStatus fromStatus, TaskRunStatus toStatus) {
        Long attemptId = IdGenerator.getInstance().nextId();
        Long taskId = IdGenerator.getInstance().nextId();
        String taskName = "task name";
        TaskAttemptStatusChangeEvent event = new TaskAttemptStatusChangeEvent(attemptId, fromStatus, toStatus, taskName, taskId);
        return event;
    }

}
