package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.Extractor;

import java.util.ArrayList;
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
            ArrayList<String> tables = (ArrayList<String>) client.getCollections(this.database);
            return Iterators.concat(tables.stream()
                    .filter(collection -> !collection.startsWith("_"))
                    .map(tableName -> new ArangoCollectionExtractor(this.dataSource, this.database, tableName).extract())
                    .iterator());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            client.close();
        }
    }
}
