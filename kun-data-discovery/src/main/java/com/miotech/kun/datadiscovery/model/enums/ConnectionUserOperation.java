package com.miotech.kun.datadiscovery.model.enums;

import com.miotech.kun.security.common.UserOperation;

public enum ConnectionUserOperation implements UserOperation {
    READ_CONN, USE_CONN, EDIT_CONN;

    @Override
    public String getName() {
        return name();
    }
}
