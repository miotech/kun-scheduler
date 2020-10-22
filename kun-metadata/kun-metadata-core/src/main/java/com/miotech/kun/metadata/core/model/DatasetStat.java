package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DatasetStat implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335281861L;

    private final long rowCount;

    private final LocalDateTime statDate;

    private final LocalDateTime lastUpdatedTime;

    public long getRowCount() {
        return rowCount;
    }

    public LocalDateTime getStatDate() {
        return statDate;
    }

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public DatasetStat(long rowCount, LocalDateTime statDate, LocalDateTime lastUpdatedTime) {
        this.rowCount = rowCount;
        this.statDate = statDate;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private long rowCount;
        private LocalDateTime statDate;
        private LocalDateTime lastUpdatedTime;

        private Builder() {
        }

        public Builder withRowCount(long rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder withStatDate(LocalDateTime statDate) {
            this.statDate = statDate;
            return this;
        }

        public Builder withLastUpdatedTime(LocalDateTime lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
            return this;
        }

        public DatasetStat build() {
            return new DatasetStat(rowCount, statDate, lastUpdatedTime);
        }
    }
}