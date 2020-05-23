package com.miotech.kun.common.taskrun.dao;

import com.miotech.kun.workflow.core.model.taskrun.TaskRun;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskRunDao {
    public TaskRun fetchLatestTaskRun(Long id) {
        // TODO: implement this method
        checkNotNull(id, "id should not be null.");
        return null;
    }

    public void createTaskRuns(List<TaskRun> taskRuns) {
        // TODO: implement this method
        return;
    }
}
