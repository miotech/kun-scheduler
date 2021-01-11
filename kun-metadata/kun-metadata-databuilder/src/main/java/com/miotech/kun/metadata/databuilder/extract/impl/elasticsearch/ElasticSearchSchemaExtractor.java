package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.client.ElasticSearchClient;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ElasticSearchSchemaExtractor extends ElasticsearchExistenceExtractor implements DatasetSchemaExtractor {

    @Override
    public List<DatasetField> extract(Dataset dataset, DataSource dataSource) {
        ElasticSearchIndexSchemaExtractor elasticSearchIndexSchemaExtractor = null;
        try {
            elasticSearchIndexSchemaExtractor =
                    new ElasticSearchIndexSchemaExtractor((ElasticSearchDataSource) dataSource, dataset.getName());
            return elasticSearchIndexSchemaExtractor.getSchema();
        } finally {
            if (elasticSearchIndexSchemaExtractor != null) {
                elasticSearchIndexSchemaExtractor.close();
            }
        }
    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) throws Exception {
        ElasticSearchClient elasticSearchClient = null;
        try {
            ElasticSearchDataSource elasticSearchDataSource = (ElasticSearchDataSource) dataSource;
            elasticSearchClient = new ElasticSearchClient((ElasticSearchDataSource) dataSource);
            Request request = new Request("GET", "_alias");
            Response response = elasticSearchClient.performRequest(request);

            Map<String, Object> indexMap = JSONUtils.jsonStringToMap(EntityUtils.toString(response.getEntity()));
            Set<String> indices = indexMap.keySet().stream()
                    .filter(index -> !index.startsWith("."))
                    .collect(Collectors.toSet());

            return Iterators.concat(indices.stream().map(index ->
                    new ElasticSearchIndexSchemaExtractor(elasticSearchDataSource, index).extract()).iterator());
        } finally {
            if (elasticSearchClient != null) {
                elasticSearchClient.close();
            }
        }
    }

}
