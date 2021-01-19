package com.miotech.kun.dataplatform.model.backfill;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = DataBackfillGroup.DataBackfillGroupBuilder.class)
public class DataBackfillGroup {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    private final List<Task> tasks;

    private final String commitMsg;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getCommitMsg() {
        return commitMsg;
    }

    private DataBackfillGroup(DataBackfillGroupBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.tasks = ImmutableList.copyOf(builder.tasks);
        this.commitMsg = builder.commitMsg;
    }

    public DataBackfillGroupBuilder cloneBuilder() {
        DataBackfillGroupBuilder builder = new DataBackfillGroupBuilder();
        builder.id = this.id;
        builder.name = this.name;
        builder.commitMsg = this.commitMsg;
        builder.tasks = this.tasks;
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataBackfillGroup that = (DataBackfillGroup) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(tasks, that.tasks) && Objects.equals(commitMsg, that.commitMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, tasks, commitMsg);
    }

    @JsonPOJOBuilder
    public static final class DataBackfillGroupBuilder {
        private Long id;
        private String name;
        private List<Task> tasks;
        private String commitMsg;

        private DataBackfillGroupBuilder() {
        }

        public static DataBackfillGroupBuilder aDataBackfillGroup() {
            return new DataBackfillGroupBuilder();
        }

        public DataBackfillGroupBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public DataBackfillGroupBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public DataBackfillGroupBuilder withTasks(List<Task> tasks) {
            this.tasks = tasks;
            return this;
        }

        public DataBackfillGroupBuilder withCommitMsg(String commitMsg) {
            this.commitMsg = commitMsg;
            return this;
        }

        public DataBackfillGroup build() {
            return new DataBackfillGroup(this);
        }
    }
}
