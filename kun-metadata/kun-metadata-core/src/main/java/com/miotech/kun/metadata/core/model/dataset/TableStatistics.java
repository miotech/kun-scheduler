package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class TableStatistics implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335281861L;

    private final Long rowCount;

    private final Long totalByteSize;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime statDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime lastUpdatedTime;

    public Long getRowCount() {
        return rowCount;
    }

    public Long getTotalByteSize() {
        return totalByteSize;
    }

    public OffsetDateTime getStatDate() {
        return statDate;
    }

    public OffsetDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    @JsonCreator
    public TableStatistics(@JsonProperty("rowCount") Long rowCount, @JsonProperty("totalByteSize") Long totalByteSize,
                           @JsonProperty("statDate") OffsetDateTime statDate, @JsonProperty("lastUpdatedTime") OffsetDateTime lastUpdatedTime) {
        this.rowCount = rowCount;
        this.totalByteSize = totalByteSize;
        this.statDate = statDate;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withRowCount(this.rowCount)
                .withTotalByteSize(this.totalByteSize)
                .withLastUpdatedTime(this.lastUpdatedTime)
                .withStatDate(this.statDate);
    }

    public static final class Builder {
        private Long rowCount;
        private Long totalByteSize;
        private OffsetDateTime statDate;
        private OffsetDateTime lastUpdatedTime;

        private Builder() {
        }

        public Builder withRowCount(Long rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder withTotalByteSize(Long totalByteSize) {
            this.totalByteSize = totalByteSize;
            return this;
        }

        public Builder withStatDate(OffsetDateTime statDate) {
            this.statDate = statDate;
            return this;
        }

        public Builder withLastUpdatedTime(OffsetDateTime lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
            return this;
        }

        public TableStatistics build() {
            return new TableStatistics(rowCount, totalByteSize, statDate, lastUpdatedTime);
        }
    }
}