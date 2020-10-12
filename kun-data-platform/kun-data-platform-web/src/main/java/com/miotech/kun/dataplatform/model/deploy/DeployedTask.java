package com.miotech.kun.dataplatform.model.deploy;

import com.miotech.kun.dataplatform.model.commit.TaskCommit;

public class DeployedTask {

    private final Long id;

    private final String name;

    private final Long definitionId;

    private final String taskTemplateName;

    private final Long workflowTaskId;

    private final Long owner;

    private final TaskCommit taskCommit;

    private final boolean archived;

    public DeployedTask(Long id,
                        String name,
                        Long definitionId,
                        String taskTemplateName,
                        Long workflowTaskId,
                        TaskCommit taskCommit,
                        Long owner,
                        boolean archived) {
        this.id = id;
        this.name = name;
        this.definitionId = definitionId;
        this.taskTemplateName = taskTemplateName;
        this.workflowTaskId = workflowTaskId;
        this.taskCommit = taskCommit;
        this.owner = owner;
        this.archived = archived;
    }

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

    public Long getWorkflowTaskId() {
        return workflowTaskId;
    }

    public Long getOwner() {
        return owner;
    }

    public TaskCommit getTaskCommit() {
        return taskCommit;
    }

    public boolean isArchived() {
        return archived;
    }

    public static Builder newBuilder() { return new Builder(); }

    public Builder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withName(name)
                .withDefinitionId(definitionId)
                .withTaskTemplateName(taskTemplateName)
                .withWorkflowTaskId(workflowTaskId)
                .withTaskCommit(taskCommit)
                .withOwner(owner)
                .withArchived(archived);
    }

    public static final class Builder {
        private Long id;
        private String name;
        private Long definitionId;
        private String taskTemplateName;
        private Long workflowTaskId;
        private Long owner;
        private TaskCommit taskCommit;
        private boolean archived;

        private Builder() {
        }

        public static Builder aDeployedTask() {
            return new Builder();
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

        public Builder withWorkflowTaskId(Long workflowTaskId) {
            this.workflowTaskId = workflowTaskId;
            return this;
        }

        public Builder withOwner(Long owner) {
            this.owner = owner;
            return this;
        }

        public Builder withTaskCommit(TaskCommit taskCommit) {
            this.taskCommit = taskCommit;
            return this;
        }

        public Builder withArchived(boolean archived) {
            this.archived = archived;
            return this;
        }

        public DeployedTask build() {
            return new DeployedTask(id, name, definitionId, taskTemplateName, workflowTaskId, taskCommit, owner, archived);
        }
    }
}
