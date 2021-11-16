package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;

import javax.annotation.Nullable;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class DataStore implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335452366L;

    private final DataStoreType type;

    public DataStoreType getType() {
        return type;
    }

    /**
     * extract database and schema info
     * @return
     */
    @JsonIgnore
    @Nullable
    public abstract String getDatabaseName();

    public DataStore(DataStoreType type) {
        this.type = type;
    }

    /**
     * Returns data set identifier (DSI) of this datastore object
     * @return data set identifier
     */
    @JsonIgnore
    @Nullable
    public abstract String getLocationInfo();

    @JsonIgnore
    @Nullable
    /**
     * @return data store connection info
     */
    public abstract ConnectionInfo getConnectionInfo();

    @JsonIgnore
    @Nullable
    public abstract String getName();
}
