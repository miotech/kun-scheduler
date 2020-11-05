package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

public interface Executor {
    /**
     * 提交一个TaskAttempt以运行。
     *
     * @param taskAttempt
     */
    public boolean submit(TaskAttempt taskAttempt);

    /**
     * 取消一个TaskAttempt的运行。
     *
     * @param taskAttempt
     * @return
     */
    public default boolean cancel(TaskAttempt taskAttempt) {
        return cancel(taskAttempt.getId());
    }

    /**
     * 取消一个TaskAttempt的运行。
     *
     * @param taskAttemptId
     * @return
     */
    public boolean cancel(Long taskAttemptId);

    /**
     * 处理TaskAttempt执行状态变更
     *
     * @param msg
     * @return
     */
    public boolean statusUpdate(TaskAttemptMsg msg);

    /**
     * 接收来自worker的心跳信息
     *
     * @param heartBeatMessage
     * @return
     */
    public boolean heartBeatReceive(HeartBeatMessage heartBeatMessage);

    /**
     * 关闭executor
     *
     * @return
     */
    public boolean shutdown();

    /**
     * 恢复executor
     *
     * @return
     */
    public boolean recover();

}
