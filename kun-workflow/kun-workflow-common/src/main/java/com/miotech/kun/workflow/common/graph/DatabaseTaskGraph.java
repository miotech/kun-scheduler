package com.miotech.kun.workflow.common.graph;

import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DatabaseTaskGraph implements TaskGraph {
    private final TaskDao taskDao;

    @Inject
    public DatabaseTaskGraph(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public List<Task> tasksScheduledAt(Tick tick) {
        List<Task> scheduledTasks = taskDao.fetchScheduledTaskAtTick(tick);
        taskDao.updateTasksNextExecutionTick(tick, scheduledTasks);
        return scheduledTasks;
    }
}
