package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class FieldStatistics implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335393385L;

    private final String fieldName;

    private final long distinctCount;

    private final long nonnullCount;

    private final String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime statDate;

    public String getFieldName() {
        return fieldName;
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

    public OffsetDateTime getStatDate() {
        return statDate;
    }

    @JsonCreator
    public FieldStatistics(@JsonProperty("fieldName") String fieldName, @JsonProperty("distinctCount") long distinctCount,
                           @JsonProperty("nonnullCount") long nonnullCount, @JsonProperty("updatedBy") String updatedBy,
                           @JsonProperty("statDate") OffsetDateTime statDate) {
        this.fieldName = fieldName;
        this.distinctCount = distinctCount;
        this.nonnullCount = nonnullCount;
        this.updatedBy = updatedBy;
        this.statDate = statDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withFieldName(this.fieldName)
                .withDistinctCount(this.distinctCount)
                .withNonnullCount(this.nonnullCount)
                .withStatDate(this.statDate)
                .withUpdatedBy(this.updatedBy);
    }

    public static final class Builder {
        private String fieldName;
        private long distinctCount;
        private long nonnullCount;
        private String updatedBy;
        private OffsetDateTime statDate;

        public Builder withFieldName(String fieldName) {
            this.fieldName = fieldName;
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

        public Builder withStatDate(OffsetDateTime statDate) {
            this.statDate = statDate;
            return this;
        }

        public FieldStatistics build() {
            return new FieldStatistics(fieldName, distinctCount, nonnullCount, updatedBy, statDate);
        }
    }
}
