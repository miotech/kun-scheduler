package com.miotech.kun.metadata.constant;

public enum DataStoreType {
    TABLE("PostgresTable/MySQLTable/HiveTable"),
    INDEX("ElasticsearchIndex"),
    COLLECTION("ArangoCollection/MongoCollection"),
    TOPIC("KafkaTopic"),
    SHEET("GoogleSpreadSheet"),
    FILE("File");

    private String type;

    DataStoreType(String type) {
        this.type = type;
    }
}
