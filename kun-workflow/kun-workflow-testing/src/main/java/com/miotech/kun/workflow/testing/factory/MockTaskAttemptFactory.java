package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

public class MockTaskAttemptFactory {
    public static TaskAttempt createTaskAttempt() {
        return createTaskAttempt(MockTaskRunFactory.createTaskRun());
    }

    public static TaskAttempt createTaskAttempt(TaskRun taskRun) {
        return TaskAttempt.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), 1))
                .withAttempt(1)
                .withTaskRun(taskRun)
                .withStatus(TaskRunStatus.CREATED)
                .build();
    }
}
