package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;
import com.miotech.kun.metadata.databuilder.client.ElasticSearchClient;
import com.miotech.kun.metadata.databuilder.extract.statistics.DatasetStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class ElasticSearchStatisticsExtractor extends ElasticsearchExistenceExtractor implements DatasetStatisticsExtractor {

    @Override
    public Long getRowCount(Dataset dataset, DataSource dataSource) {
        ElasticSearchDataSource elasticSearchDataSource = (ElasticSearchDataSource) dataSource;
        ElasticSearchClient elasticSearchClient = null;
        try {
            elasticSearchClient = new ElasticSearchClient(elasticSearchDataSource);

            CountRequest countRequest = new CountRequest(dataset.getName());
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            countRequest.source(searchSourceBuilder);
            return elasticSearchClient.count(countRequest);
        } finally {
            if (elasticSearchClient != null) {
                elasticSearchClient.close();
            }
        }
    }

    @Override
    public List<FieldStatistics> extractFieldStatistics(Dataset dataset, DataSource dataSource) {
        ElasticSearchDataSource elasticSearchDataSource = (ElasticSearchDataSource) dataSource;
        ElasticSearchClient elasticSearchClient = null;
        try {
            elasticSearchClient = new ElasticSearchClient(elasticSearchDataSource);
            final ElasticSearchClient finalElasticSearchClient = elasticSearchClient;
            return dataset.getFields().stream().map(field -> {
                CountRequest countRequest = new CountRequest();
                String fieldName = field.getName();
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.existsQuery(fieldName));
                countRequest.source(searchSourceBuilder);
                Long count = finalElasticSearchClient.count(countRequest);
                return FieldStatistics.newBuilder()
                        .withFieldName(field.getName())
                        .withNonnullCount(count)
                        .withStatDate(DateTimeUtils.now()).build();
            }).collect(Collectors.toList());

        } finally {
            if (elasticSearchClient != null) {
                elasticSearchClient.close();
            }
        }
    }
}
