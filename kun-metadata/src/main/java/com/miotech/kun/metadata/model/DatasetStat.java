package com.miotech.kun.metadata.model;

import java.util.Date;

public class DatasetStat {

    private final long rowCount;

    private final Date statDate;

    public long getRowCount() {
        return rowCount;
    }

    public Date getStatDate() {
        return statDate;
    }

    public DatasetStat(long rowCount, Date statDate) {
        this.rowCount = rowCount;
        this.statDate = statDate;
    }

    public static DatasetStat.Builder newBuilder() {
        return new DatasetStat.Builder();
    }

    public static final class Builder {
        private long rowCount;
        private Date statDate;

        private Builder() {
        }

        public static Builder aDatasetStat() {
            return new Builder();
        }

        public Builder withRowCount(long rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder withStatDate(Date statDate) {
            this.statDate = statDate;
            return this;
        }

        public DatasetStat build() {
            return new DatasetStat(rowCount, statDate);
        }
    }
}
