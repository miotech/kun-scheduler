package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;

import java.util.Collection;
import java.util.Iterator;

public class ArangoDatabaseExtractor implements Extractor {

    private ArangoDataSource dataSource;
    private String database;
    private ArangoClient client;

    public ArangoDatabaseExtractor(ArangoDataSource dataSource, String database) {
        this.dataSource = dataSource;
        this.database = database;
        this.client = new ArangoClient(dataSource);
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            Collection<String> tables = client.getCollections(this.database);
            return Iterators.concat(tables.stream()
                    .filter(collection -> !collection.startsWith("_"))
                    .map(tableName -> new ArangoCollectionExtractor(this.dataSource, this.database, tableName).extract())
                    .iterator());
        } finally {
            client.close();
        }
    }
}
