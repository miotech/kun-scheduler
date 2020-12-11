package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;

import java.util.List;
import java.util.Objects;

public class TaskRunTemplate {

    private final Long taskRunId;

    private final Task task;

    private final Config config;

    private final Tick scheduledTick;

    private final TaskRunTemplateStatus status;

    private final List<Long> dependentTaskRunIds;

    public TaskRunTemplate(Long taskRunId, Task task, Config config, Tick scheduledTick,
                           TaskRunTemplateStatus status, List<Long> dependentTaskRunIds) {
        this.taskRunId = taskRunId;
        this.task = task;
        this.config = config;
        this.scheduledTick = scheduledTick;
        this.status = status;
        this.dependentTaskRunIds = dependentTaskRunIds;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public Task getTask() {
        return task;
    }

    public Config getConfig() {
        return config;
    }

    public Tick getScheduledTick() {
        return scheduledTick;
    }

    public TaskRunTemplateStatus getStatus() {
        return status;
    }

    public List<Long> getDependentTaskRunIds() {
        return dependentTaskRunIds;
    }

    public TaskRun toTaskRun(){
        return TaskRun.newBuilder()
                .withId(taskRunId)
                .withScheduledTick(scheduledTick)
                .withTask(task)
                .withConfig(config)
                .withDependentTaskRunIds(dependentTaskRunIds)
                .build();
    }

    public TaskRunTemplateBuilder cloneBuilder(){
        return TaskRunTemplate.newBuilder()
                .withTaskRunId(taskRunId)
                .withScheduledTick(scheduledTick)
                .withTask(task)
                .withConfig(config)
                .withDependentTaskRunIds(dependentTaskRunIds);
    }

    public static TaskRunTemplateBuilder newBuilder(){
        return new TaskRunTemplateBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskRunTemplate)) return false;
        TaskRunTemplate that = (TaskRunTemplate) o;
        return getTaskRunId().equals(that.getTaskRunId()) &&
                Objects.equals(getTask(), that.getTask()) &&
                Objects.equals(getConfig(), that.getConfig()) &&
                Objects.equals(getScheduledTick(), that.getScheduledTick()) &&
                getStatus() == that.getStatus() &&
                Objects.equals(getDependentTaskRunIds(), that.getDependentTaskRunIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskRunId(), getTask(), getConfig(), getScheduledTick(), getStatus(), getDependentTaskRunIds());
    }


    public static final class TaskRunTemplateBuilder {
        private Long taskRunId;
        private Task task;
        private Config config;
        private Tick scheduledTick;
        private TaskRunTemplateStatus status;
        private List<Long> dependentTaskRunIds;

        private TaskRunTemplateBuilder() {
        }

        public static TaskRunTemplateBuilder aTaskRunTemplate() {
            return new TaskRunTemplateBuilder();
        }

        public TaskRunTemplateBuilder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public TaskRunTemplateBuilder withTask(Task task) {
            this.task = task;
            return this;
        }

        public TaskRunTemplateBuilder withConfig(Config config) {
            this.config = config;
            return this;
        }

        public TaskRunTemplateBuilder withScheduledTick(Tick scheduledTick) {
            this.scheduledTick = scheduledTick;
            return this;
        }

        public TaskRunTemplateBuilder withStatus(TaskRunTemplateStatus status) {
            this.status = status;
            return this;
        }

        public TaskRunTemplateBuilder withDependentTaskRunIds(List<Long> dependentTaskRunIds) {
            this.dependentTaskRunIds = dependentTaskRunIds;
            return this;
        }

        public TaskRunTemplate build() {
            return new TaskRunTemplate(taskRunId, task, config, scheduledTick, status, dependentTaskRunIds);
        }
    }
}
