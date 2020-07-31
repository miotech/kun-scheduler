package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.execution.TaskAttemptReport;

public class ExecResult {
    private boolean success;
    private boolean cancelled;
    private TaskAttemptReport report;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public TaskAttemptReport getReport() {
        return report;
    }

    public void setReport(TaskAttemptReport report) {
        this.report = report;
    }
}
