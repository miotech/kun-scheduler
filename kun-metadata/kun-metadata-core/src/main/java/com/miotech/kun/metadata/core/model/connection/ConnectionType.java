package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ConnectionType {
    HIVE_THRIFT,
    GLUE,
    MYSQL,
    POSTGRESQL,
    PRESTO,
    ATHENA,
    HIVE_SERVER,
    S3,
    HDFS,
    ARANGO,
    MONGODB,
    ELASTICSEARCH;

    @JsonCreator
    public static ConnectionType fromString(String type){
        return valueOf(type);
    }

    public boolean isJdbc(){
        return this == HIVE_THRIFT || this == MYSQL || this == POSTGRESQL || this == ATHENA;
    }
}
