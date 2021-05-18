package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.util.Map;

public enum DependencyStatus {
    CREATED,
    FAILED,
    SUCCESS;

    private static Map<String, DependencyStatus> statusMaps = ImmutableMap.of(CREATED.name(), CREATED,
            FAILED.name(), FAILED, SUCCESS.name(), SUCCESS);

    public static DependencyStatus resolve(String status) {
        return statusMaps.get(status);
    }

    public static DependencyStatus fromUpstreamStatus(TaskRunStatus taskRunStatus){
        if(taskRunStatus.isSuccess()){
            return SUCCESS;
        }
        if(taskRunStatus.isFailure()){
            return FAILED;
        }
        return CREATED;
    }
}
