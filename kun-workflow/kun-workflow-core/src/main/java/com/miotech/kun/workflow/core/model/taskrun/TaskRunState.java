package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;

/**
 * Define the behavior for taskRun status transition
 */
public interface TaskRunState {

    /**
     * make taskRun status move to next status by event
     * @param taskRunTransitionEvent
     * @return
     */
    TaskRunStatus doTransition(TaskRunTransitionEvent taskRunTransitionEvent);

    TaskRunStatus getStatus();

}
