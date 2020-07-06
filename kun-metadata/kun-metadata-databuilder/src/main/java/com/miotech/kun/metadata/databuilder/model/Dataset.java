package com.miotech.kun.metadata.databuilder.model;

import com.miotech.kun.workflow.core.model.lineage.DataStore;

import javax.annotation.Nullable;
import java.util.List;

public class Dataset {

    private final Long datasourceId;

    private final String name;

    private final DataStore dataStore;

    private final List<DatasetField> fields;

    private final List<DatasetFieldStat> fieldStats;

    private final DatasetStat datasetStat;

    public Long getDatasourceId() {
        return datasourceId;
    }

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

    @Nullable
    public String getDatabaseName() {
        return dataStore.getDatabaseName();
    }

    public Dataset(Long datasourceId, String name, DataStore dataStore, List<DatasetField> fields, List<DatasetFieldStat> fieldStats, DatasetStat datasetStat) {
        this.datasourceId = datasourceId;
        this.name = name;
        this.dataStore = dataStore;
        this.fields = fields;
        this.fieldStats = fieldStats;
        this.datasetStat = datasetStat;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withName(name)
                .withDataStore(dataStore)
                .withFields(fields)
                .withFieldStats(fieldStats)
                .withDatasetStat(datasetStat);
    }

    public static final class Builder {
        private Long datasourceId;
        private String name;
        private DataStore dataStore;
        private List<DatasetField> fields;
        private List<DatasetFieldStat> fieldStats;
        private DatasetStat datasetStat;

        private Builder() {
        }

        public Builder withDatasourceId(Long datasourceId) {
            this.datasourceId = datasourceId;
            return this;
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
            return new Dataset(datasourceId, name, dataStore, fields, fieldStats, datasetStat);
        }
    }
}
