package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
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

public class MioElasticSearchClient {
    private static Logger logger = LoggerFactory.getLogger(MioElasticSearchClient.class);

    private RestHighLevelClient highLevelClient;
    private RestClient lowLevelClient;

    public MioElasticSearchClient(ElasticSearchDataSource dataSource) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(dataSource.getUsername(), dataSource.getPassword()));
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(120000);
        requestBuilder.setConnectionRequestTimeout(120000);

        RestClientBuilder builder = RestClient.builder(new HttpHost(dataSource.getUrl().split(":")[0],
                Integer.parseInt(dataSource.getUrl().split(":")[1]),
                "http"))
                .setHttpClientConfigCallback(
                        httpClientBuilder -> httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setDefaultRequestConfig(requestBuilder.build())
                );
        this.highLevelClient = new RestHighLevelClient(builder);
        this.lowLevelClient = builder.build();
    }

    public RestHighLevelClient getHighLevelClient() {
        return highLevelClient;
    }

    public RestClient getLowLevelClient() {
        return lowLevelClient;
    }

    public Response performRequest(Request request) {
        try {
            return lowLevelClient.performRequest(request);
        } catch (IOException e) {
            logger.error("elasticseatch low level api call failed, query -> " + request.getEndpoint(), e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }


    public Long count(CountRequest request) {
        try {
            CountResponse response = highLevelClient.count(request, RequestOptions.DEFAULT);
            return response.getCount();
        } catch (IOException e) {
            logger.error("elasticseatch high level api call failed", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void close() {
        if (highLevelClient != null) {
            try {
                highLevelClient.close();
            } catch (IOException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }

        if (lowLevelClient != null) {
            try {
                lowLevelClient.close();
            } catch (IOException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }
    }

}
