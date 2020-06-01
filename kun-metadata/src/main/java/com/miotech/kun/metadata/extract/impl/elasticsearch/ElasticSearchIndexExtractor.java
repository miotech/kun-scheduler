package com.miotech.kun.metadata.extract.impl.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.model.CommonCluster;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.ElasticSearchIndexStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ElasticSearchIndexExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(ElasticSearchIndexExtractor.class);

    private String index;
    private CommonCluster cluster;
    private MioElasticSearchClient client;


    public ElasticSearchIndexExtractor(CommonCluster cluster, String index) {
        this.index = index;
        this.cluster = cluster;
        this.client = new MioElasticSearchClient(cluster);
    }

    @Override
    public List<DatasetField> getSchema(){
        try{
            Request request = new Request("POST","_xpack/sql");
            request.setJsonEntity(String.format("{\"query\":\"describe \\\"%s\\\"\"}", index));

            Response response = client.performRequest(request);
            String json = EntityUtils.toString(response.getEntity());
            JsonNode root = JSONUtils.stringToJson(json);

            List<DatasetField> fields = new ArrayList<>();
            if(!root.get("rows").isEmpty()){
                for(final JsonNode node : root.get("rows")){
                    Iterator<JsonNode> it = node.iterator();
                    String name = it.next().asText();
                    if (name.endsWith("keyword"))
                        continue;
                    String type = it.next().asText();
                    if(type.equals("STRUCT"))
                        continue;
                    fields.add(new DatasetField(name, type, ""));
                }
            }
            return fields;
        }catch (IOException e){
            logger.error("json parse failed", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField){

        CountRequest countRequest = new CountRequest(index);
        String fieldName = datasetField.getName();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.existsQuery(fieldName));
        countRequest.source(searchSourceBuilder);
        Long count = client.count(countRequest);

        DatasetFieldStat stat = DatasetFieldStat.newBuilder()
                .withNonnullCount(count)
                .withStatDate(new Date()).build();

        return stat;
    }


    @Override
    public DatasetStat getTableStats(){

        CountRequest countRequest = new CountRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        countRequest.source(searchSourceBuilder);
        Long rowCount = client.count(countRequest);

        DatasetStat datasetStat = DatasetStat.newBuilder()
                .withRowCount(rowCount)
                .withStatDate(new Date()).build();

        return datasetStat;
    }

    @Override
    public DataStore getDataStore(){
        DataStore dataStore = new ElasticSearchIndexStore(cluster.getDataStoreUrl(), index);
        return dataStore;
    }

    @Override
    protected String getName() {
        return index;
    }

}
