package com.miotech.kun.metadata.core.model.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.OffsetDateTime;

/**
 * An abstract model which represents the process of pulling a data source, data set, etc. in metadata module
 */
public abstract class PullProcess {
    /**
     * Id of this process
     */
    protected Long processId;

    /**
     * Create time of this process
     */
    protected OffsetDateTime createdAt;

    public Long getProcessId() { return processId; }

    /**
     * Get pull process type
     */
    public abstract PullProcessType getProcessType();

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }
}
