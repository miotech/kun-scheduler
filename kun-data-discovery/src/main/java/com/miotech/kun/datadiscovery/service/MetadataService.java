package com.miotech.kun.datadiscovery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author: Jie Chen
 * @created: 2020/8/27
 */
@Service
@Slf4j
public class MetadataService {

    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    RestTemplate restTemplate;

    public void pullDataset(Long datasetId) {
        restTemplate.postForEntity(url + "/datasets/{id}/_pull", null, Object.class, datasetId);
    }

    public void pullDataSource(Long datasourceId) {
        String fullUrl = url + "/datasources/{id}/_pull";
        log.info("Request url : " + fullUrl);
        restTemplate.postForEntity(fullUrl, null, Object.class, datasourceId);
    }
}
