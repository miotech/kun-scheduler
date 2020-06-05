package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;

public class MockTaskRunFactory {
    public static TaskRun createTaskRun(Long id, Task task, Clock mockClock) {
        return TaskRun.newBuilder()
                .withId(id)
                .withTask(task)
                .withInlets(new ArrayList<>())
                .withOutlets(new ArrayList<>())
                .withDependentTaskRunIds(new ArrayList<>())
                .withScheduledTick(new Tick(OffsetDateTime.now(mockClock)))
                .withStartAt(OffsetDateTime.now(mockClock))
                .withEndAt(OffsetDateTime.now(mockClock))
                .withStatus(TaskRunStatus.QUEUED)
                .withVariables(new ArrayList<>())
                .build();
    }

    public static TaskAttempt createTaskAttempt(Long id, TaskRun taskRun, int attempt, Clock mockClock) {
        return TaskAttempt.newBuilder()
                .withId(id)
                .withTaskRun(taskRun)
                .withAttempt(attempt)
                .withStartAt(OffsetDateTime.now(mockClock))
                .withEndAt(OffsetDateTime.now(mockClock).plusHours(1))
                .withLogPath("/var/log_" + taskRun + "_" + attempt + ".log")
                .withStatus(TaskRunStatus.RUNNING)
                .build();
    }
}
