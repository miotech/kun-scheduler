package com.miotech.kun.datadiscovery.model.enums;

import java.util.Set;

public interface KunRole {

    Set<UserOperation> getUserOperation();

    Integer rank();

    String getName();

}
