package com.miotech.kun.metadata.model;

import java.time.LocalDate;

public class DatasetStat {

    private final long rowCount;

    private final LocalDate statDate;

    public long getRowCount() {
        return rowCount;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public DatasetStat(long rowCount, LocalDate statDate) {
        this.rowCount = rowCount;
        this.statDate = statDate;
    }

    public static DatasetStat.Builder newBuilder() {
        return new DatasetStat.Builder();
    }

    public static final class Builder {
        private long rowCount;
        private LocalDate statDate;

        private Builder() {
        }

        public static Builder aDatasetStat() {
            return new Builder();
        }

        public Builder withRowCount(long rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder withStatDate(LocalDate statDate) {
            this.statDate = statDate;
            return this;
        }

        public DatasetStat build() {
            return new DatasetStat(rowCount, statDate);
        }
    }
}
