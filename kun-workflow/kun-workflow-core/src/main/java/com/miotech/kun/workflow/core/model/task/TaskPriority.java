package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum TaskPriority {
    HIGHEST(32),
    HIGH(24),
    MEDIUM(16),
    LOW(8),
    LOWEST(4);

    private static Map<String, TaskPriority> priorityMaps = ImmutableMap.of(HIGHEST.name(), HIGHEST,
            HIGH.name(), HIGH, MEDIUM.name(), MEDIUM, LOW.name(), LOW, LOWEST.name(), LOWEST);


    private final int priority;

    TaskPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static int getPriorityByName(String name){
        return priorityMaps.get(name).priority;
    }

}
