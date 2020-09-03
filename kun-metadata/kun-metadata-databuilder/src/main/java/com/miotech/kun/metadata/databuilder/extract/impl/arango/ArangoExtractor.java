package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.Extractor;

import java.util.ArrayList;
import java.util.Iterator;

public class ArangoExtractor implements Extractor {

    private ArangoDataSource dataSource;
    private ArangoClient client;

    public ArangoExtractor(ArangoDataSource dataSource){
        this.dataSource = dataSource;
        this.client = new ArangoClient(dataSource);
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            ArrayList<String> databases = (ArrayList<String>) client.getDatabases();
            return Iterators.concat(databases.stream().filter(db -> !db.startsWith("_"))
                    .map(databasesName -> new ArangoDatabaseExtractor(dataSource, databasesName).extract()).iterator());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            client.close();
        }
    }

}
