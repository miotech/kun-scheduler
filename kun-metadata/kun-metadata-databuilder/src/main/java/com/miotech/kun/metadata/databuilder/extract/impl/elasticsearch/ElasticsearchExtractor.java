package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.AbstractExtractor;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Elastic Search Extractor
 */
public class ElasticsearchExtractor extends AbstractExtractor {

    private ElasticSearchDataSource dataSource;
    private ElasticSearchClient client;

    public ElasticsearchExtractor(Props props, ElasticSearchDataSource dataSource) {
        super(props);
        this.dataSource = dataSource;
        this.client = new ElasticSearchClient(dataSource);
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            Request request = new Request("GET", "_alias");
            Response response = client.performRequest(request);

            Map<String, Object> indexMap = JSONUtils.jsonStringToMap(EntityUtils.toString(response.getEntity()));
            Set<String> indices = indexMap.keySet().stream()
                    .filter(index -> !index.startsWith("."))
                    .collect(Collectors.toSet());

            return Iterators.concat(indices.stream().map(index ->
                    new ElasticSearchIndexExtractor(getProps(), dataSource, index).extract()).iterator());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            client.close();
        }

    }

}
