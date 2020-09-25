package com.miotech.kun.dataplatform.common.commit.dao;

import lombok.EqualsAndHashCode;

public class VersionProps {

    private final long id;

    private final int version;

    private final long definitionId;

    public VersionProps(long id, int version, long definitionId) {
        this.id = id;
        this.version = version;
        this.definitionId = definitionId;
    }

    public long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    private long getDefinitionId() { return definitionId; }
}
