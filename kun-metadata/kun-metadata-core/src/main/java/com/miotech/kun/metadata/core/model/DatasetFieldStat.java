package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DatasetFieldStat implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335393385L;

    private final String name;

    private final long distinctCount;

    private final long nonnullCount;

    private final String updatedBy;

    private final LocalDateTime statDate;

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

    public LocalDateTime getStatDate() {
        return statDate;
    }

    public DatasetFieldStat(String name, long distinctCount, long nonnullCount, String updatedBy, LocalDateTime statDate) {
        this.name = name;
        this.distinctCount = distinctCount;
        this.nonnullCount = nonnullCount;
        this.updatedBy = updatedBy;
        this.statDate = statDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private long distinctCount;
        private long nonnullCount;
        private String updatedBy;
        private LocalDateTime statDate;

        public Builder withName(String name) {
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

        public Builder withStatDate(LocalDateTime statDate) {
            this.statDate = statDate;
            return this;
        }

        public DatasetFieldStat build() {
            return new DatasetFieldStat(name, distinctCount, nonnullCount, updatedBy, statDate);
        }
    }
}
