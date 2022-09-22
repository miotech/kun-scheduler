package com.miotech.kun.workflow.core.model.taskrun;

public class ActionContext {
    private final ActionType actionType;
    private final Long taskRunId;
    private final Long taskAttemptId;

    public ActionContext(ActionType actionType, Long taskRunId, Long taskAttemptId) {
        this.actionType = actionType;
        this.taskRunId = taskRunId;
        this.taskAttemptId = taskAttemptId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    public static Builder newBuilder(){
        return new Builder();
    }


    public static final class Builder {
        private ActionType actionType;
        private Long taskRunId;
        private Long taskAttemptId;

        private Builder() {
        }

        public static Builder anActionContext() {
            return new Builder();
        }

        public Builder withActionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public Builder withTaskAttemptId(Long taskAttemptId) {
            this.taskAttemptId = taskAttemptId;
            return this;
        }

        public ActionContext build() {
            return new ActionContext(actionType, taskRunId, taskAttemptId);
        }
    }
}
