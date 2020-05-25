package com.miotech.kun.metadata.constant;

public enum  DatabaseType {
    POSTGRES("Postgres",1),
    MYSQL("MySQL", 2),
    MONGO("Mongo", 5),
    ARANGO("Arango", 6),
    ELASTICSEARCH("ElasticsSearch", 7),
    PRESTO("Presto", 10),
    HIVE("Hive", 11);

    private String name;
    private int value;

    DatabaseType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

}
