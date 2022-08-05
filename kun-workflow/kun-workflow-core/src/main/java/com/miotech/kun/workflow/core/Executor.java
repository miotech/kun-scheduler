package com.miotech.kun.workflow.core;

import com.google.inject.Injector;
import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.executor.ExecutorInfo;

public interface Executor extends TaskAttemptExecutor, ResourceManager {

    /**
     * only for test
     */
    @Internal
    public void shutdown();


    /**
     * inject members
     * @param injector
     */
    public void injectMembers(Injector injector);

    /**
     * init executor
     */
    public void init();

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

    /**
     * set maintenance mode
     */
    void setMaintenanceMode(boolean mode);

    /**
     * get maintenance mode
     * @return
     */
    boolean getMaintenanceMode();

    /**
     * get info list of executor including name, label, queue info
     * @return
     */
    ExecutorInfo getExecutorInfo();

}
