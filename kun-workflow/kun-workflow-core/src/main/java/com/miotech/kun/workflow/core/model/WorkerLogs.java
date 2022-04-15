package com.miotech.kun.workflow.core.model;

import java.util.List;

public class WorkerLogs {

    private final List<String> logs;
    private final Integer startLine;
    private final Integer endLine;
    private final Integer lineCount;

    public WorkerLogs(List<String> logs, Integer startLine, Integer endLine, Integer lineCount) {
        this.logs = logs;
        this.startLine = startLine;
        this.endLine = endLine;
        this.lineCount = lineCount;
    }

    public List<String> getLogs() {
        return logs;
    }

    public Integer getStartLine() {
        return startLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    public Integer getLineCount() {
        return lineCount;
    }
}
