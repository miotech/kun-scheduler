package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.model.WorkerLogs;

import java.util.Map;

public interface StorageManager {

    /**
     * initialize storage manager
     */
    void init(Map<String, String> storageConfig);

    /**
     * upload operator jar to share storage
     *
     * @param operatorId
     * @param localFile  operator file name
     * @return
     */
    void uploadOperator(Long operatorId, String localFile);

    /**
     * write execute command to share storage
     *
     * @param execCommand
     * @return
     */
    ExecCommand writeExecCommand(ExecCommand execCommand);

    /**
     * get worker log from share storage
     */
    WorkerLogs workerLog(Long taskAttemptId, Integer startLine, Integer endLine);

}
