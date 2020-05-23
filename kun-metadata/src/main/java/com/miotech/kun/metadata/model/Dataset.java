package com.miotech.kun.metadata.model;

import com.miotech.kun.workflow.core.model.entity.DataStore;

import java.util.List;

public class Dataset {

    private final String name;

    private final DataStore dataStore;

    private final List<DatasetField> fields;

    private final List<DatasetFieldStat> fieldStats;

    private final DatasetStat datasetStat;

    public String getName() {
        return name;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public List<DatasetField> getFields() {
        return fields;
    }

    public List<DatasetFieldStat> getFieldStats() {
        return fieldStats;
    }

    public DatasetStat getDatasetStat() {
        return datasetStat;
    }

    public Dataset(String name, DataStore dataStore, List<DatasetField> fields, List<DatasetFieldStat> fieldStats, DatasetStat datasetStat) {
        this.name = name;
        this.dataStore = dataStore;
        this.fields = fields;
        this.fieldStats = fieldStats;
        this.datasetStat = datasetStat;
    }

    public static Dataset.Builder newBuilder() {
        return new Dataset.Builder();
    }

    public static final class Builder {
        private String name;
        private DataStore dataStore;
        private List<DatasetField> fields;
        private List<DatasetFieldStat> fieldStats;
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

        public Builder withFieldStats(List<DatasetFieldStat> fieldStats) {
            this.fieldStats = fieldStats;
            return this;
        }

        public Builder withDatasetStat(DatasetStat datasetStat) {
            this.datasetStat = datasetStat;
            return this;
        }

        public Dataset build() {
            return new Dataset(name, dataStore, fields, fieldStats, datasetStat);
        }
    }
}
