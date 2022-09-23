package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.model.common.Condition;

import java.time.OffsetDateTime;

public class TaskRunCondition {

    private final Condition condition;

    private final boolean result;

    private final ConditionType type;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime updatedAt;

    public Condition getCondition() {
        return condition;
    }

    public boolean getResult() {
        return result;
    }

    public ConditionType getType() {
        return type;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public TaskRunCondition(Condition condition, boolean result, ConditionType type, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.condition = condition;
        this.result = result;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public TaskRunCondition onSuccess(){
        return cloneBuilder().withResult(true).build();
    }

    public TaskRunCondition onFailed(){
        if(type.equals(ConditionType.TASKRUN_PREDECESSOR_FINISH)){
            return cloneBuilder().withResult(true).build();
        }
        return this;
    }

    public TaskRunCondition onUpstreamFailed(){
        if(type.equals(ConditionType.TASKRUN_PREDECESSOR_FINISH)){
            return cloneBuilder().withResult(true).build();
        }
        return this;
    }

    public TaskRunCondition onScheduling(){
        return cloneBuilder().withResult(false).build();
    }

    public static TaskRunConditionBuilder newBuilder() {
        return new TaskRunConditionBuilder();
    }

    public TaskRunConditionBuilder cloneBuilder() {
        return newBuilder().
                withCondition(condition)
                .withResult(result)
                .withType(type)
                .withCreatedAt(createdAt)
                .withUpdatedAt(updatedAt);
    }

    @Override
    public String toString() {
        return "TaskRunCondition{" +
                "condition='" + condition + '\'' +
                ", result=" + result +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


    public static final class TaskRunConditionBuilder {
        private Condition condition;
        private boolean result;
        private ConditionType type;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        private TaskRunConditionBuilder() {
        }

        public TaskRunConditionBuilder withCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public TaskRunConditionBuilder withResult(boolean result) {
            this.result = result;
            return this;
        }

        public TaskRunConditionBuilder withType(ConditionType type) {
            this.type = type;
            return this;
        }

        public TaskRunConditionBuilder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskRunConditionBuilder withUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TaskRunCondition build() {
            return new TaskRunCondition(condition, result, type, createdAt, updatedAt);
        }
    }
}
