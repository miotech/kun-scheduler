package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.vo.PullProcessVO;
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

    public PullProcessVO pullDataset(Long datasetId) {
        String fullUrl = url + "/datasets/{id}/_pull";
        log.info("Request url : " + fullUrl);
        return restTemplate
                .postForEntity(fullUrl, null, PullProcessVO.class, datasetId)
                .getBody();
    }

    public PullProcessVO pullDataSource(Long datasourceId) {
        String fullUrl = url + "/datasources/{id}/_pull";
        log.info("Request url : " + fullUrl);
        return restTemplate
                .postForEntity(fullUrl, null, PullProcessVO.class, datasourceId)
                .getBody();
    }
}
