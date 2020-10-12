package com.miotech.kun.dataplatform.model.datastore;

public class TaskDataset {
    private final Long id;

    private final Long definitionId;

    private final Long datastoreId;

    private final String datasetName;

    private TaskDataset(Long id,
                        Long definitionId,
                        Long datastoreId,
                        String datasetName) {
        this.id = id;
        this.definitionId = definitionId;
        this.datastoreId = datastoreId;
        this.datasetName = datasetName;
    }

    public Long getId() {
        return id;
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

    public static Builder newBuilder() { return new Builder(); }

    public Builder cloneBuilder() { return new Builder()
            .withId(id)
            .withDefinitionId(definitionId)
            .withDatastoreId(datastoreId)
            .withDatasetName(datasetName); }

    public static final class Builder {
        private Long id;
        private Long definitionId;
        private Long datastoreId;
        private String datasetName;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
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

        public TaskDataset build() {
            return new TaskDataset(id, definitionId, datastoreId, datasetName);
        }
    }
}
