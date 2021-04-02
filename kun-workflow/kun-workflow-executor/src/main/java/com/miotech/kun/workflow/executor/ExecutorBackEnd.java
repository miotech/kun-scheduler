package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;

public interface ExecutorBackEnd {

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
}
