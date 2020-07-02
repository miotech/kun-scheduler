package com.miotech.kun.metadata.databuilder.extract.impl.configurable;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.databuilder.model.ConfigurableDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.model.GlueCatalog;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.extract.iterator.GlueTableIterator;
import io.prestosql.jdbc.$internal.guava.collect.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class GlueExtractor implements Extractor {
    private final ConfigurableDataSource dataSource;

    public GlueExtractor(ConfigurableDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {
        GlueCatalog catalog = (GlueCatalog) dataSource.getCatalog();
        GlueTableIterator glueTableIterator = new GlueTableIterator(catalog.getAccessKey(),
                catalog.getSecretKey(), catalog.getRegion());

        return Iterators.concat(Streams.stream(glueTableIterator).map(table -> new GlueTableExtractor(dataSource, table).extract()).iterator());
    }
}
