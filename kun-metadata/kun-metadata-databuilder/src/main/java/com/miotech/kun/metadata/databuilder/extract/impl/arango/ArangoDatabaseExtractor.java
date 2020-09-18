package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.extract.AbstractExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;

import java.util.Collection;
import java.util.Iterator;

public class ArangoDatabaseExtractor extends AbstractExtractor {

    private ArangoDataSource dataSource;
    private String dbName;
    private ArangoClient client;

    public ArangoDatabaseExtractor(Props props, ArangoDataSource dataSource, String dbName) {
        super(props);
        this.dataSource = dataSource;
        this.dbName = dbName;
        this.client = new ArangoClient(dataSource);
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            Collection<String> tables = client.getCollections(this.dbName);
            return Iterators.concat(tables.stream()
                    .filter(collection -> !collection.startsWith("_"))
                    .map(tableName -> new ArangoCollectionExtractor(getProps(), this.dataSource, this.dbName, tableName)
                            .extract()
                    ).iterator());
        } finally {
            client.close();
        }
    }
}
