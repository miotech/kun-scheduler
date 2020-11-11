package com.miotech.kun.workflow.core.model.task;

import com.miotech.kun.workflow.core.model.common.Tick;

import java.util.List;

public interface TaskGraph {
    /**
     * 返回当前时刻需要生成实例的Task列表。需要保证Task列表是根据依赖关系拓扑排序过的。
     * @param tick
     * @return
     */
    public List<Task> tasksScheduledAt(Tick tick);

    public void updateTasksNextExecutionTick(Tick tick,List<Task> scheduledTasks);
}
