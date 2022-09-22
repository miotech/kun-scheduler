package com.miotech.kun.workflow.core.model.taskrun;

/**
 * Define action when taskRun state transit
 */
public interface TaskRunAction {

    /**
     * do action before taskRun state transit
     * @param taskRunSMMessage
     */
    void run(TaskRunSMMessage taskRunSMMessage);

}
