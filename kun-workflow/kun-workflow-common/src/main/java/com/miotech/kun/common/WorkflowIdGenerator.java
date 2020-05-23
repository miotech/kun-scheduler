package com.miotech.kun.common;

import com.miotech.kun.commons.utils.IdGenerator;

import javax.inject.Singleton;

@Singleton
public class WorkflowIdGenerator {
    private final IdGenerator idGenerator;

    public WorkflowIdGenerator() {
        this.idGenerator = IdGenerator.getInstance();
    }

    public long nextTaskId() {
        return idGenerator.nextId();
    }

    public long nextTaskRunId() {
        return idGenerator.nextId();
    }

    public long nextTaskAttemptId(long taskRunId, int attempt) {
        return idGenerator.combine(taskRunId, attempt);
    }
}
