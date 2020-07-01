package com.miotech.kun.metadata.databuilder.model;

import java.time.LocalDateTime;

public class DatasetStat {

    private final long rowCount;

    private final LocalDateTime statDate;

    public long getRowCount() {
        return rowCount;
    }

    public LocalDateTime getStatDate() {
        return statDate;
    }

    public DatasetStat(long rowCount, LocalDateTime statDate) {
        this.rowCount = rowCount;
        this.statDate = statDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private long rowCount;
        private LocalDateTime statDate;

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

        public DatasetStat build() {
            return new DatasetStat(rowCount, statDate);
        }
    }
}
