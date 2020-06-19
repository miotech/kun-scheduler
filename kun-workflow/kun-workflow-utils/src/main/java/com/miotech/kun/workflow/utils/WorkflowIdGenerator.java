package com.miotech.kun.workflow.utils;

import com.miotech.kun.commons.utils.IdGenerator;

public class WorkflowIdGenerator {
    private WorkflowIdGenerator() {
    }

    /**
     * Generate a snowflake ID for workflow Operator
     * @return Snowflake ID
     */
    public static long nextOperatorId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for workflow Task
     * @return Snowflake ID
     */
    public static long nextTaskId() {
        return IdGenerator.getInstance().nextId();
    }

    /**
     * Generate a snowflake ID for workflow TaskRun
     * @return Snowflake ID
     */
    public static long nextTaskRunId() {
        return IdGenerator.getInstance().nextId();
    }


    /**
     * Generate a customized snowflake ID for workflow Task Attempt
     * @param taskRunId TaskRun instance ID that current TaskRunAttempt instance belongs to
     * @param attempt ordinal of current attempt
     * @return Snowflake ID with attempt as reserved id
     */
    public static long nextTaskAttemptId(long taskRunId, int attempt) {
        return IdGenerator.getInstance().combine(taskRunId, attempt);
    }

    public static long taskRunIdFromTaskAttemptId(long taskAttemptId) {
        return IdGenerator.getInstance().split(taskAttemptId)[0];
    }
}
