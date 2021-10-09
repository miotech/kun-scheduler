package com.miotech.kun.metadata.core.model.dataset;

import java.time.OffsetDateTime;

public class DatasetSnapshot {

    private final Long id;

    private final Long datasetGid;

    private final SchemaSnapshot schemaSnapshot;

    private final StatisticsSnapshot statisticsSnapshot;

    private final OffsetDateTime schemaAt;

    private final OffsetDateTime statisticsAt;

    public DatasetSnapshot(Long id, Long datasetGid, SchemaSnapshot schemaSnapshot, StatisticsSnapshot statisticsSnapshot,
                           OffsetDateTime schemaAt, OffsetDateTime statisticsAt) {
        this.id = id;
        this.datasetGid = datasetGid;
        this.schemaSnapshot = schemaSnapshot;
        this.statisticsSnapshot = statisticsSnapshot;
        this.schemaAt = schemaAt;
        this.statisticsAt = statisticsAt;
    }

    public Long getId() {
        return id;
    }

    public Long getDatasetGid() {
        return datasetGid;
    }

    public SchemaSnapshot getSchemaSnapshot() {
        return schemaSnapshot;
    }

    public StatisticsSnapshot getStatisticsSnapshot() {
        return statisticsSnapshot;
    }

    public OffsetDateTime getSchemaAt() {
        return schemaAt;
    }

    public OffsetDateTime getStatisticsAt() {
        return statisticsAt;
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Long datasetGid;
        private SchemaSnapshot schemaSnapshot;
        private StatisticsSnapshot statisticsSnapshot;
        private OffsetDateTime schemaAt;
        private OffsetDateTime statisticsAt;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withDatasetGid(Long datasetGid) {
            this.datasetGid = datasetGid;
            return this;
        }

        public Builder withSchemaSnapshot(SchemaSnapshot schemaSnapshot) {
            this.schemaSnapshot = schemaSnapshot;
            return this;
        }

        public Builder withStatisticsSnapshot(StatisticsSnapshot statisticsSnapshot) {
            this.statisticsSnapshot = statisticsSnapshot;
            return this;
        }

        public Builder withSchemaAt(OffsetDateTime schemaAt) {
            this.schemaAt = schemaAt;
            return this;
        }

        public Builder withStatisticsAt(OffsetDateTime statisticsAt) {
            this.statisticsAt = statisticsAt;
            return this;
        }

        public DatasetSnapshot build() {
            return new DatasetSnapshot(id, datasetGid, schemaSnapshot, statisticsSnapshot, schemaAt, statisticsAt);
        }
    }
}
