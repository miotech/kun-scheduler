package com.miotech.kun.datadiscovery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadiscovery.model.vo.PullProcessVO;
import com.miotech.kun.metadata.core.model.DatasetColumnSuggestRequest;
import com.miotech.kun.metadata.core.model.DatasetColumnSuggestResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/8/27
 */
@Service("DataDiscoveryMetadataService")
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

    public Map<String, PullProcessVO> fetchLatestPullProcessByDataSourceIds(List<Long> datasourceIds) {
        String fullUrl = url + String.format("/datasources/_pull/latest?dataSourceIds=%s",
                StringUtils.join(datasourceIds.stream().map(Object::toString).collect(Collectors.toList()), ","));
        log.info("Request url : " + fullUrl);
        String json = restTemplate.getForObject(fullUrl, String.class);
        Map<String, PullProcessVO> result;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.readValue(json, new TypeReference<Map<String, PullProcessVO>>() {});
        } catch (Exception e) {
            log.error("Failed to converting json \"{}\" to map", json, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        return result;
    }

    public Optional<PullProcessVO> fetchLatestPullProcessForDataset(Long datasetId) {
        String fullUrl = url + "/datasets/{datasetId}/_pull/latest";
        log.info("Request url : " + fullUrl);
        return Optional.ofNullable(restTemplate.getForObject(fullUrl, PullProcessVO.class, datasetId));
    }

    public List<String> suggestDatabase(String prefix) {
        String suggestDatabaseUrl = url + "/dataset/database/_suggest";
        if (StringUtils.isNotBlank(prefix)) {
            suggestDatabaseUrl = suggestDatabaseUrl + "?prefix=" + prefix;
        }
        return Arrays.asList(restTemplate.getForEntity(suggestDatabaseUrl, String[].class).getBody());
    }

    public List<String> suggestTable(String databaseName, String prefix) {
        String suggestTableUrl = url + "/dataset/table/_suggest?databaseName=" + databaseName;
        if (StringUtils.isNotBlank(prefix)) {
            suggestTableUrl = suggestTableUrl + "&prefix=" + prefix;
        }
        return Arrays.asList(restTemplate.getForEntity(suggestTableUrl, String[].class).getBody());
    }

    public List<DatasetColumnSuggestResponse> suggestColumn(List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        String suggestColumnUrl = url + "/dataset/column/_suggest";
        return Arrays.asList(restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(columnSuggestRequests), DatasetColumnSuggestResponse[].class).getBody());
    }
}
