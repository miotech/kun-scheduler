package com.miotech.kun.metadata.databuilder.model;

public class LoadSchemaResult {

    private final long gid;

    private final long snapshotId;

    public LoadSchemaResult(long gid, long snapshotId) {
        this.gid = gid;
        this.snapshotId = snapshotId;
    }

    public long getGid() {
        return gid;
    }

    public long getSnapshotId() {
        return snapshotId;
    }
}
