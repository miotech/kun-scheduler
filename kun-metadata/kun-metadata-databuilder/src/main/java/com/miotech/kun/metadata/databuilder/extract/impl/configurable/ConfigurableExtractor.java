package com.miotech.kun.metadata.databuilder.extract.impl.configurable;

import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveExtractor;
import com.miotech.kun.metadata.databuilder.model.Catalog;
import com.miotech.kun.metadata.databuilder.model.ConfigurableDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;

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
        switch (catalogType) {
            case META_STORE:
                return new HiveExtractor(dataSource).extract();
            case GLUE:
                return new GlueExtractor(dataSource).extract();
            default:
                throw new IllegalArgumentException("Invalid Catalog.Type: " + catalogType);
        }
    }

}
