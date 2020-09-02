package com.miotech.kun.metadata.core.model;

public enum DataStoreType {

    // Table
    HIVE_TABLE,
    MYSQL_TABLE,
    POSTGRES_TABLE,
    // ElasticsearchIndex
    ELASTICSEARCH_INDEX,
    // Collection
    ARANGO_COLLECTION,
    MONGO_COLLECTION,
    // KafkaTopic
    TOPIC,
    // GoogleSpreadsheet
    SHEET,
    // File
    FILE;
}
