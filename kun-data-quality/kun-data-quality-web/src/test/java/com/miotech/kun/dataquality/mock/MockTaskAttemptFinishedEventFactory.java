package com.miotech.kun.dataquality.mock;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.util.List;

public class MockTaskAttemptFinishedEventFactory {

    private MockTaskAttemptFinishedEventFactory() {
    }

    public static TaskAttemptFinishedEvent create(TaskRunStatus taskRunStatus) {
        Long taskId = WorkflowIdGenerator.nextTaskId();
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long attemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);

        return create(attemptId, taskId, taskRunId, taskRunStatus, Lists.newArrayList(), Lists.newArrayList());
    }

    private static TaskAttemptFinishedEvent create(Long attemptId, Long taskId, Long taskRunId, TaskRunStatus finalStatus,
                                                   List<DataStore> inlets, List<DataStore> outlets) {
        return new TaskAttemptFinishedEvent(attemptId, taskId, taskRunId, finalStatus, inlets, outlets);
    }

}
