package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.Extractor;

import java.util.ArrayList;
import java.util.Iterator;

public class ArangoDatabaseExtractor implements Extractor {

    private ArangoDataSource cluster;
    private String database;
    private ArangoClient client;

    public ArangoDatabaseExtractor(ArangoDataSource cluster, String database) {
        this.cluster = cluster;
        this.database = database;
        this.client = new ArangoClient(cluster);
    }

    @Override
    public Iterator<Dataset> extract() {
        ArrayList<String> tables = (ArrayList<String>) client.getCollections(this.database);
        return Iterators.concat(tables.stream()
                .filter(collection -> !collection.startsWith("_"))
                .map(tableName -> new ArangoCollectionExtractor(this.cluster, this.database, tableName)
                        .extract()
                ).iterator());
    }
}
