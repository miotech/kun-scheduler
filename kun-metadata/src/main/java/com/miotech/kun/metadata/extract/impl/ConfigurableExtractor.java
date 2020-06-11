package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.extract.impl.hive.HiveExtractor;
import com.miotech.kun.metadata.model.*;

import java.util.Iterator;

public class ConfigurableExtractor implements Extractor {

    private final ConfigurableDataSource dataSource;

    public ConfigurableExtractor(ConfigurableDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ConfigurableDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {

        Catalog.Type catalogType = dataSource.getCatalog().getType();
        switch (catalogType){
            case MetaStore:
                return new HiveExtractor(dataSource).extract();
            case Glue:
                return new GlueExtractor(dataSource).extract();
            default:
                throw new IllegalArgumentException("Invalid Catalog.Type: " + catalogType);
        }
    }

}
