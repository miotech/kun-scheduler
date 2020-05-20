package com.miotech.kun.workflow.utils;

import com.miotech.kun.commons.utils.IdGenerator;

public class TaskIdGenerator {
    private TaskIdGenerator() {
    }

    /**
     * Generate a snowflake ID for workflow Task
     * @return Snowflake ID
     */
    public static Long nextTaskId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for workflow TaskRun
     * @return Snowflake ID
     */
    public static Long nextTaskRunId() {
        return IdGenerator.getInstance().nextId();
    }


    /**
     * Generate a customized snowflake ID for workflow Task Attempt
     * @param taskRunId TaskRun instance ID that current TaskRunAttempt instance belongs to
     * @param attempt ordinal of current attempt
     * @return Snowflake ID with attempt as reserved id
     */
    public static Long nextTaskAttemptId(long taskRunId, int attempt) {
        return IdGenerator.combine(taskRunId, attempt);
    }
}
