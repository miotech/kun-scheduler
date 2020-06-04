package com.miotech.kun.metadata.model;

public abstract class Catalog {

    private final Type type;

    public Catalog(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public abstract String toString();

    public enum Type {
        Glue,
        MetaStore
    }

}
