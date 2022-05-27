package com.miotech.kun.security.common;

import java.util.Set;

public interface KunRole {

    Set<UserOperation> getUserOperation();

    Integer rank();

    String getName();

}
