package com.miotech.kun.monitor.alert.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class MockTaskAttemptStatusChangeEventFactory {

    private MockTaskAttemptStatusChangeEventFactory() {
    }

    public static TaskAttemptStatusChangeEvent create(boolean isSuccess) {
        long attemptId = 1L;
        long taskId = IdGenerator.getInstance().nextId();
        if (isSuccess) {
            TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
            return eventSuccess;
        } else {
            TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);
            return eventFailed;
        }
    }


}
