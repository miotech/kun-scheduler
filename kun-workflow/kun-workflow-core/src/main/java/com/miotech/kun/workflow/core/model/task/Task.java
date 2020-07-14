package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.common.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = Task.TaskBuilder.class)
public class Task {
    private final Long id;

    private final String name;

    private final String description;

    private final Long operatorId;

    private final List<Param> arguments;

    private final List<Variable> variableDefs;

    private final ScheduleConf scheduleConf;

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

    public List<Param> getArguments() {
        return arguments;
    }

    public List<Variable> getVariableDefs() {
        return variableDefs;
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
        this.arguments = ImmutableList.copyOf(builder.arguments);
        this.variableDefs = ImmutableList.copyOf(builder.variableDefs);
        this.scheduleConf = builder.scheduleConf;
        this.dependencies = ImmutableList.copyOf(builder.dependencies);
        this.tags = builder.tags;
    }

    public TaskBuilder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withName(name)
                .withDescription(description)
                .withOperatorId(operatorId)
                .withArguments(arguments)
                .withVariableDefs(variableDefs)
                .withScheduleConf(scheduleConf)
                .withDependencies(dependencies)
                .withTags(tags);
    }

    /**
     * Convert tags list of this task instance to key-value map data structure
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
                Objects.equals(arguments, task.arguments) &&
                Objects.equals(variableDefs, task.variableDefs) &&
                Objects.equals(scheduleConf, task.scheduleConf) &&
                Objects.equals(dependencies, task.dependencies) &&
                Objects.equals(tags, task.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, operatorId, arguments, variableDefs, scheduleConf, dependencies, tags);
    }

    public static final class TaskBuilder {
        private Long id;
        private String name;
        private String description;
        private Long operatorId;
        private List<Param> arguments;
        private List<Variable> variableDefs;
        private ScheduleConf scheduleConf;
        private List<TaskDependency> dependencies;
        private List<Tag> tags;

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

        public TaskBuilder withArguments(List<Param> arguments) {
            this.arguments = arguments;
            return this;
        }

        public TaskBuilder withVariableDefs(List<Variable> variableDefs) {
            this.variableDefs = variableDefs;
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

        public Task build() {
            return new Task(this);
        }
    }
}
