package com.miotech.kun.metadata.model;

import java.util.List;

public class Dataset {

    private String name;

    private DataStore dataStore;

    private List<DatasetField> fields;

    private DatasetStat datasetStat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<DatasetField> getFields() {
        return fields;
    }

    public void setFields(List<DatasetField> fields) {
        this.fields = fields;
    }

    public DatasetStat getDatasetStat() {
        return datasetStat;
    }

    public void setDatasetStat(DatasetStat datasetStat) {
        this.datasetStat = datasetStat;
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

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDataStore(DataStore dataStore) {
            this.dataStore = dataStore;
            return this;
        }

        public Builder setFields(List<DatasetField> fields) {
            this.fields = fields;
            return this;
        }

        public Builder setDatasetStat(DatasetStat datasetStat) {
            this.datasetStat = datasetStat;
            return this;
        }

        public Dataset build() {
            Dataset dataset = new Dataset();
            dataset.setName(name);
            dataset.setDataStore(dataStore);
            dataset.setFields(fields);
            dataset.setDatasetStat(datasetStat);
            return dataset;
        }
    }
}
