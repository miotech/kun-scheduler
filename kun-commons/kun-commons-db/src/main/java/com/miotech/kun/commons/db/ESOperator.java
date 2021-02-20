package com.miotech.kun.commons.db;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.HashMap;
import java.util.Map;

public class ESOperator {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private final ThreadLocal<Map<String, String>> oldSourcesInTrans;

    public ESOperator(String host, int port, String username, String password) {
        Preconditions.checkState(StringUtils.isNotBlank(host), "`host` cannot be blank");
        Preconditions.checkState(port >= 0 && port <= 65535, "Out of legal range of port: 0 - 65535");
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.oldSourcesInTrans = ThreadLocal.withInitial(HashMap::new);
    }

    public void update(String index, String type, String id, String source) {
        try (RestHighLevelClient highLevelClient = createClient(username, password, host, port)) {
            snapshot(index, type, id);

            update(highLevelClient, createUpdateRequest(index, type, id, source));
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void rollback() {
        try (RestHighLevelClient highLevelClient = createClient(username, password, host, port)) {
            for (Map.Entry<String, String> oldSources : oldSourcesInTrans.get().entrySet()) {
                Map<String, String> indexInfo = parseKey(oldSources.getKey());
                String source = oldSources.getValue();
                update(highLevelClient, createUpdateRequest(indexInfo.get("index"), indexInfo.get("type"), indexInfo.get("id"), source));
            }
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private void snapshot(String index, String type, String id) {
        Map<String, String> oldSourceMap = oldSourcesInTrans.get();
        if (!oldSourceMap.containsKey(generateKey(index, type, id))) {
            try (RestHighLevelClient highLevelClient = createClient(username, password, host, port)) {
                oldSourceMap.put(generateKey(index, type, id), get(highLevelClient, createGetRequest(index, type, id)).getSourceAsString());
            } catch (Exception e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }
    }

    private GetRequest createGetRequest(String index, String type, String id) {
        return new GetRequest(index, type, id);
    }

    private UpdateRequest createUpdateRequest(String index, String type, String id, String source) {
        UpdateRequest updateRequest = new UpdateRequest(index, type, id);
        updateRequest.doc(source, XContentType.JSON);
        return updateRequest;
    }

    private RestHighLevelClient createClient(String username, String password, String host, int port) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(120000);
        requestBuilder.setConnectionRequestTimeout(120000);

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, "http"))
                .setHttpClientConfigCallback(
                        httpClientBuilder -> httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setDefaultRequestConfig(requestBuilder.build())
                );
        return new RestHighLevelClient(builder);
    }

    private void update(RestHighLevelClient highLevelClient, UpdateRequest updateRequest) {
        try {
            highLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private GetResponse get(RestHighLevelClient highLevelClient, GetRequest getRequest) {
        try {
            return highLevelClient.get(getRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void close() {
        oldSourcesInTrans.remove();
    }

    private String generateKey(String index, String type, String id) {
        return String.format("%s:%s:%s", index, type, id);
    }

    private Map<String, String> parseKey(String key) {
        Map<String, String> indexInfoMap = Maps.newHashMap();
        String[] infos = key.split(":");
        indexInfoMap.put("index", infos[0]);
        indexInfoMap.put("type", infos[1]);
        indexInfoMap.put("id", infos[2]);

        return indexInfoMap;
    }

}
