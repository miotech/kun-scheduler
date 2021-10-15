package com.miotech.kun.metadata.databuilder.extract.impl.arangodb;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;
import com.miotech.kun.metadata.databuilder.client.ArangoClient;
import com.miotech.kun.metadata.databuilder.extract.statistics.DatasetStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.util.List;
import java.util.stream.Collectors;

public class ArangoDBStatisticsExtractor extends ArangoDBExistenceExtractor implements DatasetStatisticsExtractor {

    @Override
    public Long getRowCount(Dataset dataset, DataSource dataSource) {
        ArangoDataSource arangoDataSource = (ArangoDataSource) dataSource;
        ArangoClient arangoClient = null;
        try {
            arangoClient = new ArangoClient(arangoDataSource);
            String query = String.format("RETURN LENGTH(%s)", dataset.getName());
            return arangoClient.count(dataset.getDatabaseName(), query).longValue();
        } finally {
            if (arangoClient != null) {
                arangoClient.close();
            }
        }
    }

    @Override
    public List<FieldStatistics> extractFieldStatistics(Dataset dataset, DataSource dataSource) {
        ArangoDataSource arangoDataSource = (ArangoDataSource) dataSource;
        ArangoClient arangoClient = null;
        try {
            arangoClient = new ArangoClient(arangoDataSource);
            final ArangoClient finalArangoClient = arangoClient;

            return dataset.getFields().stream().map(field -> {
                String query = String.format("FOR c IN %s FILTER c.%s != NULL COLLECT WITH COUNT INTO length RETURN length", dataset.getName(), field.getName());
                Integer count = finalArangoClient.count(dataset.getDatabaseName(), query);
                return FieldStatistics.newBuilder()
                        .withFieldName(field.getName())
                        .withNonnullCount(count)
                        .withStatDate(DateTimeUtils.now()).build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            if (arangoClient != null) {
                arangoClient.close();
            }
        }
    }
}
