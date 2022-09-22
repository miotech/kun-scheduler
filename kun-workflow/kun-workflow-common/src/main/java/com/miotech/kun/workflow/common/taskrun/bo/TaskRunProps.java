package com.miotech.kun.workflow.common.taskrun.bo;


import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;

/**
 * taskRun value object mapping to database
 */
public class TaskRunProps {

    private final Long id;

    private final Config config;

    private final Tick scheduledTick;

    private final ScheduleType scheduleType;

    private final TaskRunStatus status;

    private final OffsetDateTime queuedAt;

    private final OffsetDateTime startAt;

    private final OffsetDateTime endAt;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime updatedAt;

    private final Integer priority;

    private final String queueName;

    private final ExecuteTarget executeTarget;

    private final Integer taskRunPhase;

    public TaskRunProps(Long id, Config config, Tick scheduledTick, ScheduleType scheduleType, TaskRunStatus status, OffsetDateTime queuedAt, OffsetDateTime startAt, OffsetDateTime endAt
            , OffsetDateTime createdAt, OffsetDateTime updatedAt, Integer priority, String queueName, ExecuteTarget executeTarget, Integer taskRunPhase) {
        this.id = id;
        this.config = config;
        this.scheduledTick = scheduledTick;
        this.scheduleType = scheduleType;
        this.status = status;
        this.queuedAt = queuedAt;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.priority = priority;
        this.queueName = queueName;
        this.executeTarget = executeTarget;
        this.taskRunPhase = taskRunPhase;
    }

    public Long getId() {
        return id;
    }

    public Config getConfig() {
        return config;
    }

    public Tick getScheduledTick() {
        return scheduledTick;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public OffsetDateTime getQueuedAt() {
        return queuedAt;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getQueueName() {
        return queueName;
    }

    public ExecuteTarget getExecuteTarget() {
        return executeTarget;
    }

    public Integer getTaskRunPhase() {
        return taskRunPhase;
    }

    public static TaskRunPropsBuilder newBuilder(){
        return new TaskRunPropsBuilder();
    }

    public TaskRunPropsBuilder cloneBuilder(){
        return new TaskRunPropsBuilder()
                .withId(id)
                .withConfig(config)
                .withScheduledTick(scheduledTick)
                .withStatus(status)
                .withQueuedAt(queuedAt)
                .withStartAt(startAt)
                .withEndAt(endAt)
                .withCreatedAt(createdAt)
                .withUpdatedAt(updatedAt)
                .withScheduleType(scheduleType)
                .withQueueName(queueName)
                .withPriority(priority)
                .withExecuteTarget(executeTarget);
    }




    public static final class TaskRunPropsBuilder {
        private Long id;
        private Config config;
        private Tick scheduledTick;
        private ScheduleType scheduleType;
        private TaskRunStatus status;
        private OffsetDateTime queuedAt;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private Integer priority;
        private String queueName;
        private ExecuteTarget executeTarget;
        private Integer taskRunPhase;


        public TaskRunPropsBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskRunPropsBuilder withConfig(Config config) {
            this.config = config;
            return this;
        }

        public TaskRunPropsBuilder withScheduledTick(Tick scheduledTick) {
            this.scheduledTick = scheduledTick;
            return this;
        }

        public TaskRunPropsBuilder withScheduleType(ScheduleType scheduledType) {
            this.scheduleType = scheduledType;
            return this;
        }

        public TaskRunPropsBuilder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public TaskRunPropsBuilder withQueuedAt(OffsetDateTime queuedAt) {
            this.queuedAt = queuedAt;
            return this;
        }

        public TaskRunPropsBuilder withStartAt(OffsetDateTime startAt) {
            this.startAt = startAt;
            return this;
        }

        public TaskRunPropsBuilder withEndAt(OffsetDateTime endAt) {
            this.endAt = endAt;
            return this;
        }

        public TaskRunPropsBuilder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskRunPropsBuilder withUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TaskRunPropsBuilder withPriority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public TaskRunPropsBuilder withQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public TaskRunPropsBuilder withExecuteTarget(ExecuteTarget executeTarget) {
            this.executeTarget = executeTarget;
            return this;
        }

        public TaskRunPropsBuilder withTaskRunPhase(Integer taskRunPhase){
            this.taskRunPhase = taskRunPhase;
            return this;
        }

        public TaskRunProps build() {
            return new TaskRunProps(id, config, scheduledTick, scheduleType, status, queuedAt, startAt, endAt, createdAt, updatedAt, priority, queueName, executeTarget, taskRunPhase);
        }
    }
}
