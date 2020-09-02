package com.miotech.kun.workflow.core.model.lineage;

public abstract class Schema {
    private final SchemaType type;

    public Schema(SchemaType schemaType) {
        type = schemaType;
    }
}
