package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HDFSConnectionInfo extends ConnectionInfo{
    @JsonCreator
    public HDFSConnectionInfo(@JsonProperty("connectionType") ConnectionType connectionType) {
        super(connectionType);
    }
}
