package com.miotech.kun.metadata.extract.impl.elasticsearch;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.ElasticSearchDataSource;
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
    private static Logger logger = LoggerFactory.getLogger(ElasticsearchExtractor.class);

    private ElasticSearchDataSource cluster;
    private MioElasticSearchClient client;

    public ElasticsearchExtractor(ElasticSearchDataSource cluster){
        this.cluster = cluster;
        this.client = new MioElasticSearchClient(cluster);
    }

    @Override
    public Iterator<Dataset> extract() {
        Request request = new Request("GET","_alias");
        Response response = client.performRequest(request);
        String json = null;
        try {
            json = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> indexMap = JSONUtils.jsonStringToMap(json);
        Set<String> indices = indexMap.keySet().stream()
                .filter(index -> !index.startsWith("."))
                .filter(index -> !index.contains("test"))
                .collect(Collectors.toSet());

        return Iterators.concat(indices.stream().map((index) ->
                new ElasticSearchIndexExtractor(cluster, index).extract()).iterator());
    }

}
