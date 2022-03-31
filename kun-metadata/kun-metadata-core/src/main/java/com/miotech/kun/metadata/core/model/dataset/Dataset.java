package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    private final Boolean deleted;

    private final List<DatasetField> fields;

    private final List<FieldStatistics> fieldStatistics;

    private final TableStatistics tableStatistics;

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

    public Boolean isDeleted() {
        return deleted;
    }

    public List<DatasetField> getFields() {
        return fields;
    }

    public List<FieldStatistics> getFieldStatistics() {
        return fieldStatistics;
    }

    public TableStatistics getTableStatistics() {
        return tableStatistics;
    }

    @JsonIgnore
    public String getDSI(){
        return datasourceId + ":" + getDataStore().getLocationInfo();
    }

    @Nullable
    @JsonIgnore
    public String getDatabaseName() {
        return dataStore.getDatabaseName();
    }

    @JsonCreator
    public Dataset(@JsonProperty("gid") Long gid,
                   @JsonProperty("datasourceId") Long datasourceId,
                   @JsonProperty("name") String name,
                   @JsonProperty("dataStore") DataStore dataStore,
                   @JsonProperty("deleted") Boolean deleted,
                   @JsonProperty("fields") List<DatasetField> fields,
                   @JsonProperty("fieldStatistics") List<FieldStatistics> fieldStatistics,
                   @JsonProperty("tableStatistics") TableStatistics tableStatistics) {
        this.gid = gid;
        this.datasourceId = datasourceId;
        this.name = name;
        this.dataStore = dataStore;
        this.deleted = deleted;
        this.fields = fields;
        this.fieldStatistics = fieldStatistics;
        this.tableStatistics = tableStatistics;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withGid(gid)
                .withDatasourceId(datasourceId)
                .withName(name)
                .withDataStore(dataStore)
                .withDeleted(deleted)
                .withFields(fields)
                .withFieldStatistics(fieldStatistics)
                .withTableStatistics(tableStatistics);
    }

    @Override
    public String toString() {
        return "Dataset{" +
                "gid=" + gid +
                ", datasourceId=" + datasourceId +
                ", name='" + name + '\'' +
                ", dataStore=" + dataStore +
                ", deleted=" + deleted +
                '}';
    }

    public static final class Builder {
        private Long gid;
        private Long datasourceId;
        private String name;
        private DataStore dataStore;
        private Boolean deleted;
        private List<DatasetField> fields;
        private List<FieldStatistics> fieldStatistics;
        private TableStatistics tableStatistics;

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

        public Builder withDeleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder withFields(List<DatasetField> fields) {
            this.fields = fields;
            return this;
        }

        public Builder withFieldStatistics(List<FieldStatistics> fieldStatistics) {
            this.fieldStatistics = fieldStatistics;
            return this;
        }

        public Builder withTableStatistics(TableStatistics tableStatistics) {
            this.tableStatistics = tableStatistics;
            return this;
        }

        public Dataset build() {
            return new Dataset(gid, datasourceId, name, dataStore, deleted, fields, fieldStatistics, tableStatistics);
        }
    }
}
