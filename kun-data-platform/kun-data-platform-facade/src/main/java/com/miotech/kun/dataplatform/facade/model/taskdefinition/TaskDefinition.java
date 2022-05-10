package com.miotech.kun.dataplatform.facade.model.taskdefinition;

import java.time.OffsetDateTime;

public class TaskDefinition {
    private final Long id;

    private final String name;

    private final Long definitionId;

    private final String taskTemplateName;

    private final TaskPayload taskPayload;

    private final String creator;

    private final String owner;

    private final boolean archived;

    private final String lastModifier;

    private final OffsetDateTime createTime;

    private final OffsetDateTime updateTime;


    public TaskDefinition(Long id,
                          String name,
                          Long definitionId,
                          String taskTemplateName,
                          TaskPayload taskPayload,
                          String creator,
                          String owner,
                          boolean archived,
                          String lastModifier,
                          OffsetDateTime createTime,
                          OffsetDateTime updateTime) {
        this.id = id;
        this.name = name;
        this.definitionId = definitionId;
        this.taskTemplateName = taskTemplateName;
        this.taskPayload = taskPayload;
        this.creator = creator;
        this.owner = owner;
        this.archived = archived;
        this.lastModifier = lastModifier;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /**
     * @deprecated DO NOT USE THIS GETTER EXTERNALLY.
     * Use {@code getDefinitionId()} as identifier instead
     */
    @Deprecated
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public String getTaskTemplateName() {
        return taskTemplateName;
    }

    public TaskPayload getTaskPayload() {
        return taskPayload;
    }

    public String getCreator() {
        return creator;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isArchived() {
        return archived;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public static Builder newBuilder() { return new Builder(); }

    public Builder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withName(name)
                .withDefinitionId(definitionId)
                .withTaskTemplateName(taskTemplateName)
                .withCreator(creator)
                .withOwner(owner)
                .withTaskPayload(taskPayload)
                .withArchived(archived)
                .withLastModifier(lastModifier)
                .withCreateTime(createTime)
                .withUpdateTime(updateTime);
    }

    public static final class Builder {
        private Long id;
        private String name;
        private Long definitionId;
        private String taskTemplateName;
        private TaskPayload taskPayload;
        private String creator;
        private String owner;
        private boolean archived;
        private String lastModifier;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDefinitionId(Long definitionId) {
            this.definitionId = definitionId;
            return this;
        }

        public Builder withTaskTemplateName(String taskTemplateName) {
            this.taskTemplateName = taskTemplateName;
            return this;
        }

        public Builder withTaskPayload(TaskPayload taskPayload) {
            this.taskPayload = taskPayload;
            return this;
        }

        public Builder withCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder withArchived(boolean archived) {
            this.archived = archived;
            return this;
        }

        public Builder withLastModifier(String lastModifier) {
            this.lastModifier = lastModifier;
            return this;
        }

        public Builder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public TaskDefinition build() {
            return new TaskDefinition(id, name, definitionId, taskTemplateName, taskPayload, creator, owner, archived, lastModifier, createTime, updateTime);
        }
    }
}
