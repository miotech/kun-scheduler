package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HDFSConnectionConfigInfo extends ConnectionConfigInfo {
    @JsonCreator
    public HDFSConnectionConfigInfo(@JsonProperty("connectionType") ConnectionType connectionType) {
        super(connectionType);
    }
}
