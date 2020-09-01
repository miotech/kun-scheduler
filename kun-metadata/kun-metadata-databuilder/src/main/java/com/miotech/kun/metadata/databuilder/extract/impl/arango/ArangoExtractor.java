package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.Extractor;

import java.util.ArrayList;
import java.util.Iterator;

public class ArangoExtractor implements Extractor {

    private ArangoDataSource cluster;
    private ArangoClient client;

    public ArangoExtractor(ArangoDataSource cluster){
        this.cluster = cluster;
        this.client = new ArangoClient(cluster);
    }

    @Override
    public Iterator<Dataset> extract() {
        ArrayList<String> databases = (ArrayList<String>) client.getDatabases();
        return Iterators.concat(databases.stream().filter(db -> !db.startsWith("_")).map(databasesName -> new ArangoDatabaseExtractor(cluster, databasesName).extract()).iterator());
    }

}
