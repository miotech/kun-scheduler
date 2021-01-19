package com.miotech.kun.dataplatform.model.backfill;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = Backfill.BackFillBuilder.class)
public class Backfill {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    private final List<TaskRun> taskRuns;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<TaskRun> getTaskRuns() {
        return taskRuns;
    }

    private Backfill(BackFillBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.taskRuns = ImmutableList.copyOf(builder.taskRuns);
    }

    public BackFillBuilder newBuilder() {
        return new BackFillBuilder();
    }

    public BackFillBuilder cloneBuilder() {
        BackFillBuilder builder = new BackFillBuilder();
        builder.id = this.id;
        builder.name = this.name;
        builder.taskRuns = this.taskRuns;
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Backfill that = (Backfill) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(taskRuns, that.taskRuns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, taskRuns);
    }

    @JsonPOJOBuilder
    public static final class BackFillBuilder {
        private Long id;
        private String name;
        private List<TaskRun> taskRuns;

        private BackFillBuilder() {
        }

        public BackFillBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public BackFillBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public BackFillBuilder withTaskRuns(List<TaskRun> taskRuns) {
            this.taskRuns = taskRuns;
            return this;
        }

        public Backfill build() {
            return new Backfill(this);
        }
    }
}
