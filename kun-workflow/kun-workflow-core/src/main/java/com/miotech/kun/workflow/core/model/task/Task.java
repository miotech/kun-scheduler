package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.utils.CronUtils;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;
import java.util.*;

@JsonDeserialize(builder = Task.TaskBuilder.class)
public class Task {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    private final String description;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long operatorId;

    private final Config config;

    private final ScheduleConf scheduleConf;

    private final Integer recoverTimes;

    private final List<TaskDependency> dependencies;

    private final List<Tag> tags;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public Config getConfig() {
        return config;
    }

    public ScheduleConf getScheduleConf() {
        return scheduleConf;
    }

    public List<TaskDependency> getDependencies() {
        return dependencies;
    }

    public List<Tag> getTags() {
        return tags;
    }



    private Task(TaskBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.operatorId = builder.operatorId;
        this.config = builder.config;
        this.scheduleConf = builder.scheduleConf;
        this.dependencies = ImmutableList.copyOf(builder.dependencies);
        this.tags = builder.tags;
        this.recoverTimes = builder.recoverTimes == null ? 0 : builder.recoverTimes;
    }

    public TaskBuilder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withName(name)
                .withDescription(description)
                .withOperatorId(operatorId)
                .withConfig(config)
                .withScheduleConf(scheduleConf)
                .withDependencies(dependencies)
                .withTags(tags)
                .withRecoverTimes(recoverTimes);
    }

    public boolean shouldSchedule(OffsetDateTime scheduleTime, OffsetDateTime currentTime) {
        boolean shouldSchedule = false;
        switch (scheduleConf.getType()) {
            case ONESHOT:
                return true;
            case SCHEDULED:
                String cronExpression = scheduleConf.getCronExpr();
                for (int i = 0; i <= recoverTimes; i++) {
                    if(scheduleTime.compareTo(currentTime) >= 0){
                        shouldSchedule = true;
                        break;
                    }
                    Optional<OffsetDateTime> nextExecutionTimeOptional = CronUtils.getNextExecutionTimeByCronExpr(cronExpression, scheduleTime);
                    if (nextExecutionTimeOptional.isPresent()) {
                        scheduleTime = nextExecutionTimeOptional.get();
                    } else {
                        break;
                    }
                }
                break;
        }
        return shouldSchedule;
    }

    /**
     * Convert tags list of this task instance to key-value map data structure
     *
     * @return key-value map of tags
     * @throws RuntimeException when detects duplication on tag key
     */
    @JsonIgnore
    public Map<String, String> getTagsMap() {
        Preconditions.checkNotNull(tags, "Property `tags` of task object is null");
        Map<String, String> tagsMap = new HashMap<>();
        tags.forEach(tag -> {
            if (tagsMap.containsKey(tag.getKey())) {
                throw new IllegalArgumentException(
                        String.format("Tags contains duplicated key \"%s\" with values: \"%s\" and \"%s\"",
                                tag.getKey(), tagsMap.get(tag.getKey()), tag.getValue()));
            }
            // else
            tagsMap.put(tag.getKey(), tag.getValue());
        });
        return tagsMap;
    }

    public static TaskBuilder newBuilder() {
        return new TaskBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id) &&
                Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                Objects.equals(operatorId, task.operatorId) &&
                Objects.equals(config, task.config) &&
                Objects.equals(scheduleConf, task.scheduleConf) &&
                Objects.equals(dependencies, task.dependencies) &&
                Objects.equals(tags, task.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, operatorId, config, scheduleConf, dependencies);
    }


    public static final class TaskBuilder {
        private Long id;
        private String name;
        private String description;
        private Long operatorId;
        private Config config;
        private ScheduleConf scheduleConf;
        private List<TaskDependency> dependencies;
        private List<Tag> tags;
        private Integer recoverTimes;

        private TaskBuilder() {
        }

        public TaskBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TaskBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder withOperatorId(Long operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public TaskBuilder withConfig(Config config) {
            this.config = config;
            return this;
        }

        public TaskBuilder withScheduleConf(ScheduleConf scheduleConf) {
            this.scheduleConf = scheduleConf;
            return this;
        }

        public TaskBuilder withDependencies(List<TaskDependency> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public TaskBuilder withTags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public TaskBuilder withRecoverTimes(Integer recoverTimes) {
            if (recoverTimes == null) {
                recoverTimes = 1;
            }
            recoverTimes = recoverTimes > 10 ? 10 : recoverTimes;
            this.recoverTimes = recoverTimes;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }
}
