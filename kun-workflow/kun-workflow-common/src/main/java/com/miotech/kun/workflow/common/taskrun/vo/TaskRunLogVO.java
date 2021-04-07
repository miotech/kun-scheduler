package com.miotech.kun.workflow.common.taskrun.vo;

import java.util.List;

public class TaskRunLogVO {

    private long taskRunId;

    private int attempt;

    private int startLine;

    private int endLine;

    private List<String> logs;

    public long getTaskRunId() {
        return taskRunId;
    }

    public void setTaskRunId(long taskRunId) {
        this.taskRunId = taskRunId;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

}
