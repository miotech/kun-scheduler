package com.miotech.kun.workflow.common.task.service;

import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.model.bo.RunTaskInfo;
import com.miotech.kun.workflow.core.model.task.Task;

import java.util.List;

@Singleton
public class TaskService {
    public Task runTasks(List<RunTaskInfo> runTaskInfos) {
        return Task.newBuilder().build();
    }
}
