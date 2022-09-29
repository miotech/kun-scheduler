package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.ResourceAttributeRequest;
import com.miotech.kun.datadiscovery.model.entity.Glossary;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.GlossaryResourceAttribute;
import com.miotech.kun.metadata.core.model.search.ResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MockSearchInfoFactory {


    private MockSearchInfoFactory() {
    }


    public static SearchedInfo mockDataSetSearch(String keyword, DataSetResourceAttribute resourceAttribute) {
        SearchedInfo datasetSearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(IdGenerator.getInstance().nextId())
                .withResourceType(ResourceType.DATASET)
                .withName(keyword)
                .withDescription("desc")
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false).build();
        return datasetSearchedInfo;
    }

    public static SearchedInfo mockRefTableSearch(Long id, String name, ResourceAttribute resourceAttribute) {
        SearchedInfo refTableSearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(id)
                .withResourceType(ResourceType.REF_TABLE)
                .withName(name)
                .withDescription("desc")
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false).build();
        return refTableSearchedInfo;
    }

    public static UniversalSearchInfo mockUniversalSearchInfo(List<SearchedInfo> searchedInfoList) {
        UniversalSearchInfo us = new UniversalSearchInfo(searchedInfoList);
        us.setTotalCount(searchedInfoList.size());
        return us;
    }

    public static BasicSearchRequest mockBasicSearchRequest(String keyword) {
        BasicSearchRequest basicSearchRequest = new BasicSearchRequest();
        basicSearchRequest.setKeyword(keyword);
        return basicSearchRequest;
    }


    public static UniversalSearchInfo mockSearchGlossary(Collection<Glossary> glossaryList, String keyword) {
        String upperCase = keyword.toUpperCase();
        List<SearchedInfo> collect = glossaryList.stream().map(glossary -> SearchedInfo.Builder.newBuilder()
                        .withGid(glossary.getId())
                        .withResourceType(ResourceType.GLOSSARY)
                        .withName(glossary.getName())
                        .withDescription(glossary.getDescription())
                        .withResourceAttribute(new GlossaryResourceAttribute(glossary.getUpdateUser()))
                        .build()).filter(searchedInfo -> searchedInfo.getName().toUpperCase().contains(upperCase)
                        || searchedInfo.getDescription().toUpperCase().contains(upperCase))
                .collect(Collectors.toList());
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo();
        universalSearchInfo.setSearchedInfoList(collect);
        return universalSearchInfo;
    }


    public static ResourceAttributeRequest mockDResourceAttributeRequest(String resourceAttributeName, Map<String, Object> resourceAttributeMap) {
        ResourceAttributeRequest request = new ResourceAttributeRequest();
        request.setResourceAttributeMap(resourceAttributeMap);
        request.setResourceAttributeName(resourceAttributeName);
        return request;
    }
}
