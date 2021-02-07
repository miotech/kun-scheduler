package com.miotech.kun.workflow.client.model;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RunTaskRequest {
    private Map<Long, Map<String, Object>> taskConfigs = Maps.newHashMap();

    public void addTaskConfig(Long taskId, Map<String, Object> overwriteConfig) {
        this.taskConfigs.put(taskId, overwriteConfig);
    }

    public List<RunTaskInfo> getRunTasks() {
        return this.taskConfigs.entrySet()
                .stream().map(x -> new RunTaskInfo(
                        x.getKey(),
                        x.getValue())
                )
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunTaskRequest that = (RunTaskRequest) o;
        return Objects.equals(taskConfigs, that.taskConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskConfigs);
    }
}
