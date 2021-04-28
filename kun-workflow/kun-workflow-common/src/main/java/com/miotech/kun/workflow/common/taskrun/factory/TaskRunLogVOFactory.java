package com.miotech.kun.workflow.common.taskrun.factory;

import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;

import java.util.List;

public class TaskRunLogVOFactory {
    private TaskRunLogVOFactory() {
    }

    // Create empty response value object while task run or task attempt exists but log file cannot be found
    public static TaskRunLogVO createLogNotFound(long taskRunId, int attempt) {
        TaskRunLogVO taskRunLogVO = new TaskRunLogVO();
        taskRunLogVO.setTaskRunId(taskRunId);
        taskRunLogVO.setAttempt(attempt);
        taskRunLogVO.setStartLine(0);
        taskRunLogVO.setEndLine(0);
        taskRunLogVO.setLogs(null);
        return taskRunLogVO;
    }

    public static TaskRunLogVO create(long taskRunId, int attempt, int startLine, int endLine, List<String> logs) {
        TaskRunLogVO taskRunLogVO = new TaskRunLogVO();
        taskRunLogVO.setTaskRunId(taskRunId);
        taskRunLogVO.setAttempt(attempt);
        taskRunLogVO.setStartLine(startLine);
        taskRunLogVO.setEndLine(endLine);
        taskRunLogVO.setLogs(logs);
        return taskRunLogVO;
    }
}
