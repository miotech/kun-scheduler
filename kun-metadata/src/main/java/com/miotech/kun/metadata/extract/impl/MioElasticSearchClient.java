package com.miotech.kun.metadata.extract.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.model.entity.CommonCluster;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.*;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


@Singleton
public class MioElasticSearchClient {
    private static Logger logger = LoggerFactory.getLogger(MioElasticSearchClient.class);

    private RestHighLevelClient highLevelClient;
    private RestClient lowLevelClient;

    @Inject
    public MioElasticSearchClient(CommonCluster cluster){
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(cluster.getUsername(), cluster.getPassword()));
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
            requestBuilder.setConnectTimeout(120000);
            requestBuilder.setConnectionRequestTimeout(120000);

            RestClientBuilder builder = RestClient.builder(new HttpHost(cluster.getHostname(), cluster.getPort(), "http"))
                .setHttpClientConfigCallback(
                httpClientBuilder -> httpClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(requestBuilder.build())
                );
        this.highLevelClient = new RestHighLevelClient(builder);
        this.lowLevelClient = builder.build();
    }

    public Response performRequest(Request request){
        try {
            return lowLevelClient.performRequest(request);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }


    public Long count(CountRequest request){
        try{
            CountResponse response = highLevelClient.count(request, RequestOptions.DEFAULT);
            return response.getCount();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
