package com.miotech.kun.common.taskrun.dao;

import com.miotech.kun.common.taskrun.bo.TaskAttemptInfo;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskRunDao {
    public TaskRun fetchLatestTaskRun(Long taskId) {
        // TODO: implement this method
        checkNotNull(taskId, "taskId should not be null.");
        return null;
    }

    public List<TaskAttemptInfo> fetchLatestTaskAttempt(List<Long> taskRunIds) {
        // TODO: implement this method
        return null;
    }

    public void createTaskRuns(List<TaskRun> taskRuns) {
        // TODO: implement this method
        // TODO: 同时插入task_run和task_run_relations
        return;
    }
}
