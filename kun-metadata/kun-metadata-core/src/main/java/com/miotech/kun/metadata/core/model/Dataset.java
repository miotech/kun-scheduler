package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public class Dataset implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335342544L;

    private final Long gid;

    private final Long datasourceId;

    private final String name;

    private final DataStore dataStore;

    private final boolean deleted;

    private final List<DatasetField> fields;

    private final List<DatasetFieldStat> fieldStats;

    private final DatasetStat datasetStat;

    public Long getGid() { return gid; }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public String getName() {
        return name;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public boolean isDeleted() {
        return deleted;
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
    @JsonIgnore
    public String getDatabaseName() {
        return dataStore.getDatabaseName();
    }

    public Dataset(Long gid, Long datasourceId, String name, DataStore dataStore, boolean deleted,
                   List<DatasetField> fields, List<DatasetFieldStat> fieldStats, DatasetStat datasetStat) {
        this.gid = gid;
        this.datasourceId = datasourceId;
        this.name = name;
        this.dataStore = dataStore;
        this.deleted = deleted;
        this.fields = fields;
        this.fieldStats = fieldStats;
        this.datasetStat = datasetStat;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withGid(gid)
                .withName(name)
                .withDataStore(dataStore)
                .withDeleted(deleted)
                .withFields(fields)
                .withFieldStats(fieldStats)
                .withDatasetStat(datasetStat);
    }

    public static final class Builder {
        private Long gid;
        private Long datasourceId;
        private String name;
        private DataStore dataStore;
        private boolean deleted;
        private List<DatasetField> fields;
        private List<DatasetFieldStat> fieldStats;
        private DatasetStat datasetStat;

        private Builder() {
        }

        public Builder withGid(Long gid) {
            this.gid = gid;
            return this;
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

        public Builder withDeleted(boolean deleted) {
            this.deleted = deleted;
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
            return new Dataset(gid, datasourceId, name, dataStore, deleted, fields, fieldStats, datasetStat);
        }
    }
}
