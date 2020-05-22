package com.miotech.kun.metadata.model;

import com.miotech.kun.workflow.core.model.entity.DataStore;

import java.util.List;

public class Dataset {

    private String name;

    private DataStore dataStore;

    private List<DatasetField> fields;

    private DatasetStat datasetStat;

    public String getName() {
        return name;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public List<DatasetField> getFields() {
        return fields;
    }

    public DatasetStat getDatasetStat() {
        return datasetStat;
    }

    public Dataset(String name, DataStore dataStore, List<DatasetField> fields, DatasetStat datasetStat) {
        this.name = name;
        this.dataStore = dataStore;
        this.fields = fields;
        this.datasetStat = datasetStat;
    }

    public static Dataset.Builder newBuilder() {
        return new Dataset.Builder();
    }

    public static final class Builder {
        private String name;
        private DataStore dataStore;
        private List<DatasetField> fields;
        private DatasetStat datasetStat;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDataStore(DataStore dataStore) {
            this.dataStore = dataStore;
            return this;
        }

        public Builder withFields(List<DatasetField> fields) {
            this.fields = fields;
            return this;
        }

        public Builder withDatasetStat(DatasetStat datasetStat) {
            this.datasetStat = datasetStat;
            return this;
        }

        public Dataset build() {
            return new Dataset(name, dataStore, fields, datasetStat);
        }
    }
}
