package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TableStatistics implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335281861L;

    private final Long rowCount;

    private final Long totalByteSize;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime statDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime lastUpdatedTime;

    public Long getRowCount() {
        return rowCount;
    }

    public Long getTotalByteSize() {
        return totalByteSize;
    }

    public LocalDateTime getStatDate() {
        return statDate;
    }

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public TableStatistics(Long rowCount, Long totalByteSize, LocalDateTime statDate, LocalDateTime lastUpdatedTime) {
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
        private LocalDateTime statDate;
        private LocalDateTime lastUpdatedTime;

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

        public Builder withStatDate(LocalDateTime statDate) {
            this.statDate = statDate;
            return this;
        }

        public Builder withLastUpdatedTime(LocalDateTime lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
            return this;
        }

        public TableStatistics build() {
            return new TableStatistics(rowCount, totalByteSize, statDate, lastUpdatedTime);
        }
    }
}