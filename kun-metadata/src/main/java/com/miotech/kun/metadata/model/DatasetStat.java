package com.miotech.kun.metadata.model;

import java.util.Date;

public class DatasetStat {

    private long rowCount;

    private Date statDate;

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public Date getStatDate() {
        return statDate;
    }

    public void setStatDate(Date statDate) {
        this.statDate = statDate;
    }

}
