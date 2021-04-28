package com.miotech.kun.workflow.client.model;

import java.util.List;

public class TaskRunLog {

    private long taskRunId;

    private int attempt;

    private long startLine;

    private long endLine;

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

    public long getStartLine() {
        return startLine;
    }

    public void setStartLine(long startLine) {
        this.startLine = startLine;
    }

    public long getEndLine() {
        return endLine;
    }

    public void setEndLine(long endLine) {
        this.endLine = endLine;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
