package com.miotech.kun.metadata.databuilder.extract.filter;

import com.google.common.collect.Lists;

import java.util.List;

public class HiveTableSchemaExtractFilter {
    private static final List<String> CONCERNED_TABLE_TYPES = Lists.newArrayList("MANAGED_TABLE");

    private HiveTableSchemaExtractFilter() {
    }

    public static boolean filter(String tableType) {
        return CONCERNED_TABLE_TYPES.contains(tableType);
    }

}
