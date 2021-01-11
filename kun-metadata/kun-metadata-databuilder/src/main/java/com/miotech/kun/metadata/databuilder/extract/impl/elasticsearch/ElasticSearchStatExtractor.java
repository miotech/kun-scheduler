package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.client.ElasticSearchClient;
import com.miotech.kun.metadata.databuilder.extract.stat.DatasetStatExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class ElasticSearchStatExtractor extends ElasticsearchExistenceExtractor implements DatasetStatExtractor {

    @Override
    public Dataset extract(Dataset dataset, DataSource dataSource) {
        ElasticSearchDataSource elasticSearchDataSource = (ElasticSearchDataSource) dataSource;
        ElasticSearchClient elasticSearchClient = null;
        Dataset.Builder resultBuilder = Dataset.newBuilder().withGid(dataset.getGid());
        try {
            elasticSearchClient = new ElasticSearchClient(elasticSearchDataSource);
            final ElasticSearchClient finalElasticSearchClient = elasticSearchClient;
            resultBuilder.withFieldStats(dataset.getFields().stream().map(field -> {
                CountRequest countRequest = new CountRequest();
                String fieldName = field.getName();
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.existsQuery(fieldName));
                countRequest.source(searchSourceBuilder);
                Long count = finalElasticSearchClient.count(countRequest);
                return DatasetFieldStat.newBuilder()
                        .withFieldName(field.getName())
                        .withNonnullCount(count)
                        .withStatDate(LocalDateTime.now()).build();
            }).collect(Collectors.toList()));

            CountRequest countRequest = new CountRequest(dataset.getName());
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            countRequest.source(searchSourceBuilder);
            Long rowCount = finalElasticSearchClient.count(countRequest);
            resultBuilder.withDatasetStat(DatasetStat.newBuilder()
                    .withRowCount(rowCount)
                    .withLastUpdatedTime(getLastUpdateTime())
                    .withStatDate(LocalDateTime.now()).build());

            return resultBuilder.build();
        } finally {
            if (elasticSearchClient != null) {
                elasticSearchClient.close();
            }
        }
    }

}
