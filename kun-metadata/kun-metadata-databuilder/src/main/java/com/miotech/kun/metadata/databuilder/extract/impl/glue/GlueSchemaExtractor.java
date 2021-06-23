package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import com.miotech.kun.metadata.databuilder.extract.filter.HiveTableSchemaExtractFilter;
import com.miotech.kun.metadata.databuilder.extract.iterator.GlueTableIterator;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import io.prestosql.jdbc.$internal.guava.collect.Iterators;
import io.prestosql.jdbc.$internal.guava.collect.Streams;

import java.util.Iterator;
import java.util.List;

public class GlueSchemaExtractor extends GlueExistenceExtractor implements DatasetSchemaExtractor {

    @Override
    public List<DatasetField> extract(Dataset dataset, DataSource dataSource) {
        Table table = GlueClient.searchTable((AWSDataSource) dataSource, dataset.getDatabaseName(), dataset.getName());
        if (table == null) {
            throw new IllegalStateException("Table not found, gid: " + dataset.getGid());
        }

        GlueTableSchemaExtractor glueTableSchemaExtractor = new GlueTableSchemaExtractor((AWSDataSource) dataSource, table);
        return glueTableSchemaExtractor.getSchema();
    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        AWSDataSource awsDataSource = (AWSDataSource) dataSource;
        GlueTableIterator glueTableIterator = new GlueTableIterator(awsDataSource.getGlueAccessKey(),
                awsDataSource.getGlueSecretKey(), awsDataSource.getGlueRegion());
        return Iterators.concat(Streams.stream(glueTableIterator)
                .filter(table -> HiveTableSchemaExtractFilter.filter(table.getTableType()))
                .map(table -> new GlueTableSchemaExtractor(awsDataSource, table).extract()).iterator());
    }

}
