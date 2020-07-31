package com.miotech.kun.workflow.client.model;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunTaskRequest {
    private Map<Long, Map<String, Object>> taskVariables = Maps.newHashMap();

    public void addTaskVariable(Long taskId, Map<String, Object> taskConfig) {
        this.taskVariables.put(taskId, taskConfig);
    }

    public List<RunTaskInfo> getRunTasks() {
        return this.taskVariables.entrySet()
                .stream().map(x -> new RunTaskInfo(
                        x.getKey(),
                        x.getValue())
                )
                .collect(Collectors.toList());
    }
}
