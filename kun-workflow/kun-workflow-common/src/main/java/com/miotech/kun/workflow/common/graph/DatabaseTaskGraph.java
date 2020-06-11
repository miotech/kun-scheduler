package com.miotech.kun.workflow.common.graph;

import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;

import javax.inject.Inject;
import java.util.List;

public class DatabaseTaskGraph implements TaskGraph {
    private final TaskDao taskDao;

    @Inject
    public DatabaseTaskGraph(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public List<Task> tasksScheduledAt(Tick tick) {
        return taskDao.fetchScheduledTaskAtTick(tick);
    }
}
