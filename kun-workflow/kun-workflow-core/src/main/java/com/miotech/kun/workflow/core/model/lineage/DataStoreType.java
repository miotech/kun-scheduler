package com.miotech.kun.workflow.core.model.lineage;

public enum DataStoreType {

    // PostgresTable/MySQLTable/HiveTable
    TABLE,
    // ElasticsearchIndex
    INDEX,
    // ArangoCollection/MongoCollection
    COLLECTION,
    // KafkaTopic
    TOPIC,
    // GoogleSpreadsheet
    SHEET,
    // File
    FILE;
}
