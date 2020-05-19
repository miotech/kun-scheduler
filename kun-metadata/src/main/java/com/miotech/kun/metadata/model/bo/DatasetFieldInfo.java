package com.miotech.kun.metadata.model.bo;

public class DatasetFieldInfo {

    private Long datasetId;

    private String name;

    private String type;

    private String description;

    public DatasetFieldInfo(Builder builder) {
        this.datasetId = builder.datasetId;
        this.name = builder.name;
        this.type = builder.type;
        this.description = builder.description;
    }

    public static class Builder {

        private Long datasetId;

        private String name;

        private String type;

        private String description;

        public Builder() {
        }

        public Builder setDatasetId(Long datasetId) {
            this.datasetId = datasetId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public DatasetFieldInfo build() {
            return new DatasetFieldInfo(this);
        }
    }

}
