package com.miotech.kun.common.service;

import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.model.bo.TaskInfo;
import com.miotech.kun.workflow.core.model.task.Task;

@Singleton
public class TaskService {
    public Task createTask(TaskInfo taskBody) {
        return Task.newBuilder().build();
    }
}
