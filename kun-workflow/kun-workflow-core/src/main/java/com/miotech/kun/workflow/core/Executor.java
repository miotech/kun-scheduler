package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.core.model.WorkerLogs;

public interface Executor extends TaskAttemptExecutor, ResourceManager {

    /**
     * 关闭executor
     *
     * @return
     */
    @Internal
    public boolean reset();

    /**
     * 恢复executor
     *
     * @return
     */
    public boolean recover();

    /**
     * 获取worker运行日志
     *
     * @return
     */
    public WorkerLogs workerLog(Long taskAttemptId, Integer startLine , Integer endLine);

    /**
     * upload operator
     */
    void uploadOperator(Long operatorId, String localFile);

}
