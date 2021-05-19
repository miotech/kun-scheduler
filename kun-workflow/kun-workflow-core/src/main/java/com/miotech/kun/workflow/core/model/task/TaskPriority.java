package com.miotech.kun.workflow.core.model.task;

public enum TaskPriority {
    HIGHEST(32),
    HIGH(24),
    MEDIUM(16),
    LOW(8),
    LOWEST(4);

    private final int priority;

    TaskPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }


}
