package com.miotech.kun.datadiscovery.model.enums;


import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SecurityModule {
    GLOSSARY(GlossaryRole.class);


    private Class<? extends KunRole> kunRoleClass;

    SecurityModule(Class<? extends KunRole> kunRoleClass) {
        this.kunRoleClass = kunRoleClass;
    }


    public Map<String, ? extends KunRole> getRoleMap() {
        Map<String, ? extends KunRole> roleMap = Arrays.stream(kunRoleClass.getEnumConstants()).collect(Collectors.toMap(KunRole::getName, v -> v));
        return roleMap;
    }
}
