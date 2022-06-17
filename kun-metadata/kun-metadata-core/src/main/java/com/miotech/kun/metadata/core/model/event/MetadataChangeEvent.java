package com.miotech.kun.metadata.core.model.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.miotech.kun.commons.pubsub.event.PublicEvent;

public class MetadataChangeEvent extends PublicEvent {

    private final EventType eventType;

    private final DataSourceType dataSourceType;

    private final long dataSourceId;

    private final String databaseName;

    private final String schemaName;

    private final String tableName;

    private final Long bizTime;

    @JsonCreator
    public MetadataChangeEvent(@JsonProperty("eventType") EventType eventType,
                               @JsonProperty("dataSourceType") DataSourceType dataSourceType,
                               @JsonProperty("dataSourceId") long dataSourceId,
                               @JsonProperty("databaseName") String databaseName,
                               @JsonProperty("schemaName") String schemaName,
                               @JsonProperty("tableName") String tableName,
                               @JsonProperty("bizTime") Long bizTime) {
        this.eventType = eventType;
        this.dataSourceType = dataSourceType;
        this.dataSourceId = dataSourceId;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.bizTime = bizTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    public long getDataSourceId() {
        return dataSourceId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public Long getBizTime() {
        return bizTime;
    }

    public enum EventType {

        CREATE_TABLE, ALTER_TABLE, DROP_TABLE;

        @JsonCreator
        public static EventType forValue(String value) {
            return valueOf(value);
        }

        @JsonValue
        public String toValue() {
            return this.name();
        }
    }

    public enum DataSourceType {

        GLUE, HIVE;

        @JsonCreator
        public static DataSourceType forValue(String value) {
            return valueOf(value);
        }

        @JsonValue
        public String toValue() {
            return this.name();
        }

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private EventType eventType;
        private DataSourceType dataSourceType;
        private long dataSourceId;
        private String databaseName;
        private String schemaName;
        private String tableName;
        private Long bizTime;

        private Builder() {
        }

        public Builder withEventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder withDataSourceType(DataSourceType dataSourceType) {
            this.dataSourceType = dataSourceType;
            return this;
        }

        public Builder withDataSourceId(long dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public Builder withDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder withSchemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder withBizTime(Long bizTime) {
            this.bizTime = bizTime;
            return this;
        }

        public MetadataChangeEvent build() {
            return new MetadataChangeEvent(eventType, dataSourceType, dataSourceId, databaseName, schemaName, tableName, bizTime);
        }
    }
}
