package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;

/**
 * Define the behavior for taskRun state transition
 */
public interface TaskRunState {

    /**
     * Make taskRun state move to next status by event,
     * this method must be reentrant
     * @param message
     * @return
     */
    TaskRunState doTransition(TaskRunSMMessage message);

    /**
     * do action after state transition
     * @return
     */
    void afterTransition(TaskRunTransitionEvent taskRunTransitionEvent, TaskAttempt taskAttempt);

    /**
     * register action to TaskRunState when receive specified event type
     * @param taskRunTransitionEventType
     * @param taskRunAction
     */
    void registerAction(TaskRunTransitionEventType taskRunTransitionEventType, TaskRunAction taskRunAction);

    Integer getPhase();

}
