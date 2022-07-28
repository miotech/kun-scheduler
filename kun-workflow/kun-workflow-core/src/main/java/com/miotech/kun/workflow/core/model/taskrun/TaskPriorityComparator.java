package com.miotech.kun.workflow.core.model.taskrun;

import java.util.Comparator;

public class TaskPriorityComparator implements Comparator<TaskAttemptInQueue> {
    @Override
    public int compare(TaskAttemptInQueue o1, TaskAttemptInQueue o2) {
        Integer priority1 = o1.getTaskAttempt().getPriority() == null ? 16 : o1.getTaskAttempt().getPriority();
        Integer priority2 = o2.getTaskAttempt().getPriority() == null ? 16 : o2.getTaskAttempt().getPriority();
        if (priority1.equals(priority2)) {
            if (o1.getEnqueueTime().equals(o2.getEnqueueTime())) {
                return o1.getTaskAttempt().getId().compareTo(o2.getTaskAttempt().getId());
            }
            return o1.getEnqueueTime().compareTo(o2.getEnqueueTime());
        }
        return priority2 - priority1;
    }
}
