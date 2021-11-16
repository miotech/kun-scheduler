package com.miotech.kun.dataquality.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class MetadataClient {

    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    RestTemplate restTemplate;

    public List<Long> fetchUpstreamTaskIds(Long datasetId) {
        String fetchUrl = url + "/dataset/task/upstream?datasetId=" + datasetId;
        return restTemplate.exchange(fetchUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Long>>() {
                }).getBody();
    }

    public List<Long> fetchDownstreamTaskIds(Long datasetId) {
        String fetchUrl = url + "/dataset/task/upstream?datasetId=" + datasetId;
        return restTemplate.exchange(fetchUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Long>>() {
                }).getBody();
    }

}
