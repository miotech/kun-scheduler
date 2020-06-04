package com.miotech.kun.workflow.common.taskrun.factory;

import com.miotech.kun.workflow.common.taskrun.vo.TaskRunStateVO;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunStateVOFactory {

    public static TaskRunStateVO create(TaskRunStatus status) {
        TaskRunStateVO vo = new TaskRunStateVO();
        vo.setStatus(status);
        return vo;
    }
}
