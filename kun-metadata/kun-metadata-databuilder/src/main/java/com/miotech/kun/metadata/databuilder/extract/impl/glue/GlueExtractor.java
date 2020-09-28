package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.extract.AbstractExtractor;
import com.miotech.kun.metadata.databuilder.extract.iterator.GlueTableIterator;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import io.prestosql.jdbc.$internal.guava.collect.Iterators;
import io.prestosql.jdbc.$internal.guava.collect.Streams;

import java.util.Iterator;

public class GlueExtractor extends AbstractExtractor {
    private final AWSDataSource dataSource;

    public GlueExtractor(Props props, AWSDataSource dataSource) {
        super(props);
        this.dataSource = dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {
        GlueTableIterator glueTableIterator = new GlueTableIterator(dataSource.getGlueAccessKey(),
                dataSource.getGlueSecretKey(), dataSource.getGlueRegion());
        return Iterators.concat(Streams.stream(glueTableIterator).map(table -> new GlueTableExtractor(getProps(), dataSource, table).extract()).iterator());
    }

}
