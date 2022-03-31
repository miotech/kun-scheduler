package com.miotech.kun.dataquality.mock;

import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;

public class MockTaskRunFactory {

    private MockTaskRunFactory() {
    }

    public static TaskRun create(TaskRunStatus taskRunStatus) {
        return TaskRun.newBuilder()
                .withId(1L)
                .withTask(MockTaskFactory.create())
                .withQueueName("default")
                .withStatus(taskRunStatus)
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now())
                .withTermAt(DateTimeUtils.now())
                .build();
    }

}
