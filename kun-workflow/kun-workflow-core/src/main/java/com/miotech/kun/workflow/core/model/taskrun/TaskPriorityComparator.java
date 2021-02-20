package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.model.task.TaskPriority;

import java.util.Comparator;

public class TaskPriorityComparator implements Comparator<TaskAttempt> {
    @Override
    public int compare(TaskAttempt o1, TaskAttempt o2) {
        Integer priority1 = o1.getPriority() == null ? TaskPriority.MEDIUM.getPriority() : o1.getPriority();
        Integer priority2 = o2.getPriority() == null ? TaskPriority.MEDIUM.getPriority() : o2.getPriority();
        return priority2 - priority1;
    }
}
