package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.extract.AbstractExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;

import java.util.Collection;
import java.util.Iterator;

public class ArangoExtractor extends AbstractExtractor {

    private ArangoDataSource dataSource;
    private ArangoClient client;

    public ArangoExtractor(Props props, ArangoDataSource dataSource) {
        super(props);
        this.dataSource = dataSource;
        this.client = new ArangoClient(dataSource);
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            Collection<String> databases = client.getDatabases();
            return Iterators.concat(databases.stream().filter(db -> !db.startsWith("_")).map(databasesName ->
                    new ArangoDatabaseExtractor(getProps(), dataSource, databasesName).extract()).iterator());
        } finally {
            client.close();
        }
    }

}
