package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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

    public DatasetDetail findById(Long id) {
        String findByIdUrl = url + String.format("/dataset/%d", id);
        return restTemplate.exchange(findByIdUrl, HttpMethod.GET, null, DatasetDetail.class).getBody();
    }

    public List<Dataset> fetchDatasetsByIds(List<Long> datasetIds) {
        String fetchUrl = url + "/dataset/basic/id_list";
        return restTemplate.exchange(fetchUrl, HttpMethod.POST, new HttpEntity<>(datasetIds),
                new ParameterizedTypeReference<List<Dataset>>() {
                }).getBody();
    }

    public Dataset fetchDatasetById(Long datasetId) {
        String fetchUrl = url + "/dataset/basic/" + datasetId;
        return restTemplate.exchange(fetchUrl, HttpMethod.GET, null, Dataset.class).getBody();
    }

}
