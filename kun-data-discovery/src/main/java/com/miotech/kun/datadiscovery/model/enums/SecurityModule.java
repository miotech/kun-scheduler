package com.miotech.kun.datadiscovery.model.enums;


import com.miotech.kun.security.common.KunRole;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SecurityModule {
    GLOSSARY(GlossaryRole.class),
    CONNECTION(ConnectionRole.class);


    private Class<? extends KunRole> kunRoleClass;

    SecurityModule(Class<? extends KunRole> kunRoleClass) {
        this.kunRoleClass = kunRoleClass;
    }


    public Map<String, KunRole> getRoleMap() {
        return Arrays.stream(kunRoleClass.getEnumConstants()).collect(Collectors.toMap(KunRole::getName, v -> v));
    }
}
