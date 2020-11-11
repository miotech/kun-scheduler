package com.miotech.kun.metadata.core.model;

import java.io.Serializable;

public enum DataStoreType implements Serializable {

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
