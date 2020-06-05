package com.miotech.kun.metadata.model;

public abstract class DataSource {

    private final long id;

    private final Type type;

    public DataSource(long id, Type type) {
        this.id = id;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public enum Type {

        Postgres,
        ElasticSearch,
        Mongo,
        Arango,
        Configurable,
        Hive

    }
}
