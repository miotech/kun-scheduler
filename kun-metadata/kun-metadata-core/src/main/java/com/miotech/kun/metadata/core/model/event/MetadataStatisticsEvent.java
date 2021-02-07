package com.miotech.kun.metadata.core.model.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class MetadataStatisticsEvent {

    private final EventType eventType;

    private final long gid;

    private final long snapshotId;

    @JsonCreator
    public MetadataStatisticsEvent(@JsonProperty("eventType") EventType eventType,
                                   @JsonProperty("gid") long gid,
                                   @JsonProperty("snapshotId") long snapshotId) {
        this.eventType = eventType;
        this.gid = gid;
        this.snapshotId = snapshotId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public long getGid() {
        return gid;
    }

    public long getSnapshotId() {
        return snapshotId;
    }

    public enum EventType {

        FIELD, TABLE;

        @JsonCreator
        public static EventType forValue(String value) {
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
        private long gid;
        private long snapshotId;

        private Builder() {
        }

        public Builder withEventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder withGid(long gid) {
            this.gid = gid;
            return this;
        }

        public Builder withSnapshotId(long snapshotId) {
            this.snapshotId = snapshotId;
            return this;
        }

        public MetadataStatisticsEvent build() {
            return new MetadataStatisticsEvent(eventType, gid, snapshotId);
        }
    }
}
