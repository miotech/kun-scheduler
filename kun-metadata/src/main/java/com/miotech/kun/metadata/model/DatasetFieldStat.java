package com.miotech.kun.metadata.model;

import java.util.Date;

public class DatasetFieldStat {

    private final String name;

    private final long distinctCount;

    private final long nonnullCount;

    private final String updatedBy;

    private final Date statDate;

    public String getName() {
        return name;
    }

    public long getDistinctCount() {
        return distinctCount;
    }

    public long getNonnullCount() {
        return nonnullCount;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Date getStatDate() {
        return statDate;
    }

    public DatasetFieldStat(String name, long distinctCount, long nonnullCount, String updatedBy, Date statDate) {
        this.name = name;
        this.distinctCount = distinctCount;
        this.nonnullCount = nonnullCount;
        this.updatedBy = updatedBy;
        this.statDate = statDate;
    }

    public static DatasetFieldStat.Builder newBuilder() {
        return new DatasetFieldStat.Builder();
    }

    public static final class Builder {
        private String name;
        private long distinctCount;
        private long nonnullCount;
        private String updatedBy;
        private Date statDate;

        public Builder() {
        }

        public Builder withDistinctCount(String name) {
            this.name = name;
            return this;
        }

        public Builder withDistinctCount(long distinctCount) {
            this.distinctCount = distinctCount;
            return this;
        }

        public Builder withNonnullCount(long nonnullCount) {
            this.nonnullCount = nonnullCount;
            return this;
        }

        public Builder withUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder withStatDate(Date statDate) {
            this.statDate = statDate;
            return this;
        }

        public DatasetFieldStat build() {
            return new DatasetFieldStat(name, distinctCount, nonnullCount, updatedBy, statDate);
        }
    }
}
