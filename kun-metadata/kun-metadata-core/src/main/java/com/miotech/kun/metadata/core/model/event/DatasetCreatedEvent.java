package com.miotech.kun.metadata.core.model.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PublicEvent;

public class DatasetCreatedEvent extends PublicEvent {

    private final Long datasetId;
    private final Long dataSourceId;
    private final String database;
    private final String table;

    @JsonCreator
    public DatasetCreatedEvent(@JsonProperty("datasetId") Long datasetId,
                               @JsonProperty("timestamp") Long timestamp,
                               @JsonProperty("dataSourceId") Long dataSourceId,
                               @JsonProperty("database") String database,
                               @JsonProperty("table") String table) {
        super(timestamp);
        this.datasetId = datasetId;
        this.dataSourceId = dataSourceId;
        this.database = database;
        this.table = table;
    }

    public DatasetCreatedEvent(Long datasetId,Long dataSourceId, String database, String table) {
        this.datasetId = datasetId;
        this.dataSourceId = dataSourceId;
        this.database = database;
        this.table = table;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    public static DatasetCreatedEvent.Builder newBuilder() {
        return new DatasetCreatedEvent.Builder();
    }

    public static final class Builder {
        private Long timestamp;
        private Long datasetId;
        private Long dataSourceId;
        private String database;
        private String table;

        private Builder() {
        }

        public static Builder aDatasetCreatedEvent() {
            return new Builder();
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withDatasetId(Long datasetId) {
            this.datasetId = datasetId;
            return this;
        }

        public Builder withDataSourceId(Long dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public Builder withDatabase(String database) {
            this.database = database;
            return this;
        }

        public Builder withTable(String table) {
            this.table = table;
            return this;
        }

        public DatasetCreatedEvent build() {
            DatasetCreatedEvent datasetCreatedEvent = new DatasetCreatedEvent(datasetId, timestamp,dataSourceId, database, table);
            return datasetCreatedEvent;
        }
    }
}
