package com.miotech.kun.metadata.extract.iterator;

import com.miotech.kun.metadata.extract.DatasetExtractor;
import com.miotech.kun.metadata.extract.factory.DatasetExtractorFactory;
import com.miotech.kun.metadata.model.Database;
import com.miotech.kun.metadata.model.Dataset;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ExtractIterator<T> implements Iterator<Dataset> {

    private List<T> tables;
    private int cursor;
    private DatasetExtractorFactory factory;
    private Database database;

    public ExtractIterator(List<T> elements, DatasetExtractorFactory factory, Database database) {
        this.tables = elements;
        this.factory = factory;
        this.database = database;
    }

    @Override
    public boolean hasNext() {
        return cursor != tables.size();
    }

    @Override
    public Dataset next() {
        int i = cursor;
        if (i >= tables.size()) {
            throw new NoSuchElementException();
        }

        DatasetExtractor extractor = factory.newInstance(tables.get(i), database);
        Dataset dataset = extractor.extract();
        cursor = i + 1;
        return dataset;
    }

}
