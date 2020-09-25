package com.miotech.kun.dataplatform.model.taskdefinition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = TaskDatasetProps.Builder.class)
public class TaskDatasetProps {
    private final Long definitionId;

    private final Long datastoreId;

    private final String datasetName;

    public TaskDatasetProps(Long definitionId, Long datastoreId, String datasetName) {
        this.definitionId = definitionId;
        this.datastoreId = datastoreId;
        this.datasetName = datasetName;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public Long getDatastoreId() {
        return datastoreId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public static final class Builder {
        private Long definitionId;
        private Long datastoreId;
        private String datasetName;

        private Builder() {
        }

        public static Builder aTaskDatasetProps() {
            return new Builder();
        }

        public Builder withDefinitionId(Long definitionId) {
            this.definitionId = definitionId;
            return this;
        }

        public Builder withDatastoreId(Long datastoreId) {
            this.datastoreId = datastoreId;
            return this;
        }

        public Builder withDatasetName(String datasetName) {
            this.datasetName = datasetName;
            return this;
        }

        public TaskDatasetProps build() {
            return new TaskDatasetProps(definitionId, datastoreId, datasetName);
        }
    }
}
