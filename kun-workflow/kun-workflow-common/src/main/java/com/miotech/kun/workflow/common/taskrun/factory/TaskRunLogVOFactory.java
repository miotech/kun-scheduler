package com.miotech.kun.workflow.common.taskrun.factory;

import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;

import java.util.List;

public class TaskRunLogVOFactory {

    public static TaskRunLogVO create(long taskRunId, int attempt, long startLine, long endLine, List<String> logs) {
        TaskRunLogVO taskRunLogVO = new TaskRunLogVO();
        taskRunLogVO.setTaskRunId(taskRunId);
        taskRunLogVO.setAttempt(attempt);
        taskRunLogVO.setStartLine(startLine);
        taskRunLogVO.setEndLine(endLine);
        taskRunLogVO.setLogs(logs);
        return taskRunLogVO;
    }
}
