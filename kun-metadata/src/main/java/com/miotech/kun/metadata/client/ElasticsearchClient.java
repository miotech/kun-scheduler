package com.miotech.kun.metadata.client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ElasticSearch Client including both high level and low level API
 * @author liutao
 */
public class ElasticsearchClient {

    private static Logger logger = LoggerFactory.getLogger(ElasticsearchClient.class);
    private static RestHighLevelClient highLevelClient;

    static {
        Config config = ConfigFactory.load();
        String hostname = config.getString("com.miotech.kun.data.collect.elasticsearch.hostname");
        int port = config.getInt("com.miotech.kun.data.collect.elasticsearch.port");
        String username = config.getString("com.miotech.kun.data.collect.elasticsearch.username");
        String password = config.getString("com.miotech.kun.data.collect.elasticsearch.password");
        int timeout = config.getInt("com.miotech.kun.data.collect.elasticsearch.timeout");


        String[] hostnames = hostname.split(",");
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        List<HttpHost> httpHostList = new ArrayList<>();
        for (String oneHostname: hostnames) {
            httpHostList.add(new HttpHost(oneHostname, port, "http"));
        }

        highLevelClient = new RestHighLevelClient(RestClient.builder(
                httpHostList.toArray(new HttpHost[httpHostList.size()]))
                .setMaxRetryTimeoutMillis(timeout)
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(timeout)
                                .setSocketTimeout(timeout)
                                .setConnectionRequestTimeout(timeout))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)));
    }

    public static List<String> getIndices(String nameRegExpr) throws IOException {
        nameRegExpr = StringUtils.isEmpty(nameRegExpr) ? "" : nameRegExpr;
        String endPointString = String.format("/_cat/indices/%s", nameRegExpr);
        InputStream inputStream = highLevelClient.getLowLevelClient()
                .performRequest(new Request("get", endPointString))
                .getEntity()
                .getContent();

        return new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.toList());
    }
}
