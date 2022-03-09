package com.miotech.kun.datadiscovery.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfo;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.GlossaryResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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
     * @param keywords and
     * @return UniversalSearchInfo
     */
    public UniversalSearchInfo fullSearch(String... keywords) {
        String fullUrl = url + "/search";
        log.info("Request url : " + fullUrl);
        UriComponents comp = UriComponentsBuilder.fromHttpUrl(fullUrl).queryParam("keywords", keywords).build();
        fullUrl = comp.toString();
        return restTemplate.exchange(fullUrl, HttpMethod.GET, null, new ParameterizedTypeReference<UniversalSearchInfo>() {
        }).getBody();
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
        saveOrUpdate(searchedInfo);
    }
}
