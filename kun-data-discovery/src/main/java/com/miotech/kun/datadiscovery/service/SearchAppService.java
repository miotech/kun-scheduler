package com.miotech.kun.datadiscovery.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.ResourceAttributeRequest;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfo;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.search.GlossaryResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.ResourceAttributeInfoRequest;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: searchService
 * @author: zemin  huang
 * @create: 2022-03-14 19:52
 **/
@Service
@Slf4j
public class SearchAppService {
    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    RestTemplate restTemplate;

    /**
     * @param keyword
     * @return UniversalSearchInfo
     */
    public UniversalSearchInfo fullSearch(String keyword) {
        String fullUrl = url + "/search/full";
        log.debug("Request url : " + fullUrl);
        if (StringUtils.isNotBlank(keyword)) {
            fullUrl = fullUrl + "?keyword=" + keyword;
        }
        return restTemplate.getForEntity(fullUrl, UniversalSearchInfo.class).getBody();
    }

    public UniversalSearchInfo searchGlossary(BasicSearchRequest searchRequest) {
        UniversalSearchRequest universalSearchRequest = getUniversalSearchRequest(Sets.newHashSet(SearchContent.values()),
                ResourceType.GLOSSARY, searchRequest, null);
        return getUniversalSearchInfo(universalSearchRequest);
    }


    public UniversalSearchInfo searchFullDataSet(DatasetSearchRequest searchRequest) {
        Map<String, Object> resourceAttributeMap = getDatasetSearchResourceAttributeMap(searchRequest);
        UniversalSearchRequest universalSearchRequest = getUniversalSearchRequest(Sets.newHashSet(SearchContent.values()),
                ResourceType.DATASET, searchRequest, resourceAttributeMap);
        return getUniversalSearchInfo(universalSearchRequest);
    }

    public UniversalSearchInfo searchDataSet(BasicSearchRequest searchRequest) {
        UniversalSearchRequest universalSearchRequest = getUniversalSearchRequest(Sets.newHashSet(SearchContent.values()),
                ResourceType.DATASET, searchRequest, null);
        return getUniversalSearchInfo(universalSearchRequest);
    }

    private static Map<String, Object> getDatasetSearchResourceAttributeMap(DatasetSearchRequest searchRequest) {
        Set<String> fieldsSet = Arrays.stream(DatasetSearchRequest.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
        Map<String, Object> map = JSONUtils.toJavaObject(JSONUtils.toJsonString(searchRequest), new TypeToken<Map<String, Object>>() {
        }.getType());
        Set<String> keySet = map.keySet();
        Collection<String> subtract = CollectionUtils.subtract(keySet, fieldsSet);
        subtract.forEach(map::remove);
        return map;
    }

    public List<String> fetchResourceAttributeList(ResourceType resourceType, ResourceAttributeRequest request) {
        ResourceAttributeInfoRequest infoRequest = new ResourceAttributeInfoRequest();
        infoRequest.setResourceType(resourceType);
        infoRequest.setResourceAttributeMap(request.getResourceAttributeMap());
        infoRequest.setResourceAttributeName(request.getResourceAttributeName());
        infoRequest.setShowDeleted(request.getShowDeleted());
        log.debug("SearchAppService fetchResourceAttributeList  request: {}", request);
        String fullUrl = url + "/search/attribute/list";
        return restTemplate.exchange(fullUrl, HttpMethod.POST, new HttpEntity<>(infoRequest), new ParameterizedTypeReference<List<String>>() {
        }).getBody();
    }

    public UniversalSearchInfo getUniversalSearchInfo(UniversalSearchRequest universalSearchRequest) {
        String fullUrl = url + "/search/options";
        return restTemplate.exchange(fullUrl, HttpMethod.POST, new HttpEntity<>(universalSearchRequest), new ParameterizedTypeReference<UniversalSearchInfo>() {
        }).getBody();
    }

    private UniversalSearchRequest getUniversalSearchRequest(Set<SearchContent> searchContents, ResourceType type, BasicSearchRequest searchRequest, Map<String, Object> resourceAttributeMap) {
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(new String[]{searchRequest.getKeyword()})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(searchContents)
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request = new UniversalSearchRequest();
        request.setResourceTypes(Sets.newHashSet(type));
        request.setSearchFilterOptions(searchFilterOptionList);
        request.setPageSize(searchRequest.getPageSize());
        request.setPageNumber(searchRequest.getPageNumber());
        if (Objects.nonNull(resourceAttributeMap)) {
            request.setResourceAttributeMap(resourceAttributeMap);
        }
        return request;
    }


    public void saveOrUpdate(SearchedInfo searchedInfo) {
        Preconditions.checkNotNull(searchedInfo, "Invalid parameter `searchedInfo `: found null object");
        Preconditions.checkNotNull(searchedInfo.getGid(), "Invalid parameter `searchedInfo gid`: found null object");
        Preconditions.checkNotNull(searchedInfo.getResourceType(), "Invalid parameter `searchedInfo resourceType`: found null object");
        Preconditions.checkNotNull(searchedInfo.getName(), "Invalid parameter `searchedInfo name`: found null object");
        String suggestColumnUrl = url + "/search/update";
        restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(searchedInfo), Void.class);

    }

    public void remove(SearchedInfo searchedInfo) {
        Preconditions.checkNotNull(searchedInfo, "Invalid parameter `searchedInfo `: found null object");
        Preconditions.checkNotNull(searchedInfo.getGid(), "Invalid parameter `searchedInfo gid`: found null object");
        Preconditions.checkNotNull(searchedInfo.getResourceType(), "Invalid parameter `searchedInfo resourceType`: found null object");
        String suggestColumnUrl = url + "/search/remove";
        restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(searchedInfo), Void.class);
    }

    @Async
    public void saveOrUpdateGlossarySearchInfo(GlossaryBasicInfo glossary) {
        SearchedInfo searchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(glossary.getId())
                .withResourceType(ResourceType.GLOSSARY)
                .withName(glossary.getName())
                .withDescription(glossary.getDescription())
                .withResourceAttribute(new GlossaryResourceAttribute(glossary.getUpdateUser()))
                .build();
        saveOrUpdate(searchedInfo);
    }

    @Async
    public void removeGlossarySearchInfo(Long id) {
        SearchedInfo searchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(id)
                .withResourceType(ResourceType.GLOSSARY)
                .withDeleted(true)
                .build();
        remove(searchedInfo);
    }


}
