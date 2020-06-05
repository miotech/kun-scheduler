package com.miotech.kun.metadata.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.ArangoDataSource;
import com.miotech.kun.metadata.model.Dataset;

import java.util.ArrayList;
import java.util.Iterator;

public class ArangoExtractor implements Extractor {

    private ArangoDataSource cluster;
    private MioArangoClient client;

    public ArangoExtractor(ArangoDataSource cluster){
        this.cluster = cluster;
        this.client = new MioArangoClient(cluster);
    }

    @Override
    public Iterator<Dataset> extract() {
        ArrayList<String> databases = (ArrayList<String>) client.getDatabases();
        return Iterators.concat(databases.stream().filter(db -> !db.startsWith("_")).map((databasesName) -> new ArangoDatabaseExtractor(cluster, databasesName).extract()).iterator());
    }

}
