package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.entity.Dataset;
import com.miotech.kun.metadata.core.model.filter.FilterRule;
import com.miotech.kun.metadata.core.model.filter.FilterRuleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @program: kun
 * @description: searchService
 * @author: zemin  huang
 * @create: 2022-03-14 19:52
 **/
@Service
@Slf4j
public class FilterRuleAppService {
    @Value("${metadata.base-url:localhost:8084}")
    String url;
    @Autowired
    private  RestTemplate restTemplate;
    @Autowired
    private  MetadataService metadataService;

    public void addDatasetStatistics(Long datasetId) {
        if (Objects.isNull(datasetId)) {
            return;
        }
        log.info("add rule dataset id:{}",datasetId);
        addFilterRule(getFilterRule(datasetId));
    }

    public void removeDatasetStatistics(Long datasetId) {
        if (Objects.isNull(datasetId)) {
            return;
        }
        log.info("remove rule dataset id:{}",datasetId);
        removeFilterRule(getFilterRule(datasetId));
    }

    private FilterRule getFilterRule(Long datasetId) {
        Dataset dataset = metadataService.findById(datasetId);
        String rule = FilterRuleType.mseRule(dataset.getType(), dataset.getDatasource(), dataset.getDatabase(), dataset.getName());
        return FilterRule.FilterRuleBuilder.builder()
                .withPositive(true)
                .withType(FilterRuleType.MSE)
                .withRule(rule)
                .build();
    }

    public void addFilterRule(FilterRule filterRule) {
        String suggestColumnUrl = url + "/filter_rule/add";
        restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(filterRule), Void.class);
    }

    public void removeFilterRule(FilterRule filterRule) {
        String suggestColumnUrl = url + "/filter_rule/remove";
        restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(filterRule), Void.class);
    }


}
