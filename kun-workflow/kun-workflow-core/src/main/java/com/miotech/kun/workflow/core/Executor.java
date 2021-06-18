package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.annotation.Internal;

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
    public String workerLog(Long taskAttemptId, Integer tailLines);

}
