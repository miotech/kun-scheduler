package com.miotech.kun.metadata.databuilder.constant;

public enum  DatabaseType {
    POSTGRES("Postgres"),
    MYSQL("MySQL"),
    MONGO("Mongo"),
    ARANGO("Arango"),
    ELASTICSEARCH("ElasticsSearch"),
    PRESTO("Presto"),
    HIVE("Hive"),
    GLUE("Glue"),
    ATHENA("Athena");

    private String name;

    DatabaseType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
