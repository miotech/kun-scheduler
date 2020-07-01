package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Elastic Search Extractor
 */
public class ElasticsearchExtractor implements Extractor {
    private ElasticSearchDataSource cluster;
    private MioElasticSearchClient client;

    public ElasticsearchExtractor(ElasticSearchDataSource cluster, MioElasticSearchClient client){
        this.cluster = cluster;
        this.client = client;
    }

    @Override
    public Iterator<Dataset> extract() {
        Request request = new Request("GET","_alias");
        Response response = client.performRequest(request);
        String json;
        try {
            json = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
        Map<String, Object> indexMap = JSONUtils.jsonStringToMap(json);
        Set<String> indices = indexMap.keySet().stream()
                .filter(index -> !index.startsWith("."))
                .collect(Collectors.toSet());

        return Iterators.concat(indices.stream().map((index) ->
                new ElasticSearchIndexExtractor(cluster, index, client).extract()).iterator());
    }

}
