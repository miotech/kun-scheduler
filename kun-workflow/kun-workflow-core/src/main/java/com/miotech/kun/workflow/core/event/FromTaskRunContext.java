package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FromTaskRunContext {

    private final Long taskRunId;
    private final Integer prePhase;
    private final Integer nextPhase;
    private Long srcTaskRunId;

    @JsonCreator
    public FromTaskRunContext(@JsonProperty("taskRunId") Long taskRunId,
                              @JsonProperty("prePhase") Integer prePhase,
                              @JsonProperty("nextPhase") Integer nextPhase,
                              @JsonProperty("srcTaskRunId") Long srcTaskRunId) {
        this.taskRunId = taskRunId;
        this.prePhase = prePhase;
        this.nextPhase = nextPhase;
        this.srcTaskRunId = srcTaskRunId;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public Integer getNextPhase() {
        return nextPhase;
    }

    public Integer getPrePhase() {
        return prePhase;
    }

    public Long getSrcTaskRunId() {
        return srcTaskRunId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withTaskRunId(taskRunId)
                .withTaskRunPhase(nextPhase)
                .withSrcTaskRunId(srcTaskRunId);
    }


    @Override
    public String toString() {
        return "FromTaskRunContext{" +
                "taskRunId=" + taskRunId +
                ", prePhase=" + prePhase +
                ", nextPhase=" + nextPhase +
                ", srcTaskRunId=" + srcTaskRunId +
                '}';
    }

    public static final class Builder {
        private Long taskRunId;
        private Integer prePhase;
        private Integer nextPhase;
        private Long srcTaskRunId;


        public Builder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public Builder withPrePhase(Integer prePhase) {
            this.prePhase = prePhase;
            return this;
        }

        public Builder withTaskRunPhase(Integer taskRunPhase) {
            this.nextPhase = taskRunPhase;
            return this;
        }

        public Builder withSrcTaskRunId(Long srcTaskRunId) {
            this.srcTaskRunId = srcTaskRunId;
            return this;
        }

        public FromTaskRunContext build() {
            return new FromTaskRunContext(taskRunId, prePhase, nextPhase, srcTaskRunId);
        }
    }
}
