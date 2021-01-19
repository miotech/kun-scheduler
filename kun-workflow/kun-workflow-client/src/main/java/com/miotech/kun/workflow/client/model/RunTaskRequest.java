package com.miotech.kun.workflow.client.model;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
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
}
