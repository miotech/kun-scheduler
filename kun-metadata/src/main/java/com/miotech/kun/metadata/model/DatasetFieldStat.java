package com.miotech.kun.metadata.model;

import java.math.BigDecimal;
import java.util.Date;

public class DatasetFieldStat {

    private long distinctCount;

    private long nonnullCount;

    private String updatedBy;

    private Date statDate;

    public long getDistinctCount() {
        return distinctCount;
    }

    public void setDistinctCount(long distinctCount) {
        this.distinctCount = distinctCount;
    }

    public long getNonnullCount() {
        return nonnullCount;
    }

    public void setNonnullCount(long nonnullCount) {
        this.nonnullCount = nonnullCount;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getStatDate() {
        return statDate;
    }

    public void setStatDate(Date statDate) {
        this.statDate = statDate;
    }


    public static final class Builder {
        private long distinctCount;
        private long nonnullCount;
        private String updatedBy;
        private Date statDate;

        private Builder() {
        }

        public static Builder getInstance() {
            return new Builder();
        }

        public Builder setDistinctCount(long distinctCount) {
            this.distinctCount = distinctCount;
            return this;
        }

        public Builder setNonnullCount(long nonnullCount) {
            this.nonnullCount = nonnullCount;
            return this;
        }

        public Builder setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder setStatDate(Date statDate) {
            this.statDate = statDate;
            return this;
        }

        public DatasetFieldStat build() {
            DatasetFieldStat datasetFieldStat = new DatasetFieldStat();
            datasetFieldStat.setDistinctCount(distinctCount);
            datasetFieldStat.setNonnullCount(nonnullCount);
            datasetFieldStat.setUpdatedBy(updatedBy);
            datasetFieldStat.setStatDate(statDate);
            return datasetFieldStat;
        }
    }
}
