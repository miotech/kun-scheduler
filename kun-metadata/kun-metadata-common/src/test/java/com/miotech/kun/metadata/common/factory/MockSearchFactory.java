package com.miotech.kun.metadata.common.factory;

import com.google.common.collect.Sets;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.ResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.ResourceAttributeInfoRequest;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: MockSearchFactory
 * @author: zemin  huang
 * @create: 2022-05-12 10:47
 **/
public class MockSearchFactory {
    public static DataSetResourceAttribute mockDataSetResourceAttribute(String type, String datasource, String database, String schema, String tags, String owners) {
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase(database)
                .withDatasource(datasource)
                .withSchema(schema)
                .withTags(tags)
                .withOwners(owners)
                .withType(type)
                .build();
        return resourceAttribute;
    }

    public static ResourceAttribute mockResourceAttribute(String owners) {
        ResourceAttribute resourceAttribute = new ResourceAttribute(owners);
        return resourceAttribute;
    }

    public static SearchedInfo mockSearchedInfo(Long gid, String name, ResourceType resourceType, String description, ResourceAttribute resourceAttribute, Boolean isDeleted) {
        SearchedInfo searchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(gid)
                .withResourceType(resourceType)
                .withName(name)
                .withDescription(description)
                .withResourceAttribute(resourceAttribute)
                .withDeleted(isDeleted)
                .build();
        return searchedInfo;
    }

    public static UniversalSearchRequest mockUniversalSearchRequest(Set<ResourceType> resourceTypeHashSet, String keword, HashSet<SearchContent> searchContentHashSet, Map<String, Object> resourceAttributeMap, Boolean showDeleted) {
        UniversalSearchRequest request = new UniversalSearchRequest();
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(new String[]{keword})
                .map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(searchContentHashSet).withKeyword(s).build())
                .collect(Collectors.toList());
        request.setSearchFilterOptions(searchFilterOptionList);
        request.setResourceAttributeMap(resourceAttributeMap);
        request.setResourceTypes(resourceTypeHashSet);
        request.setShowDeleted(showDeleted);
        return request;


    }

    public static ResourceAttributeInfoRequest mockResourceAttributeInfoRequest(ResourceType resourceType, String resourceAttributeName, Map<String, Object> resourceAttributeMap, boolean showDeleted) {
        ResourceAttributeInfoRequest request = new ResourceAttributeInfoRequest();
        request.setResourceType(resourceType);
        request.setResourceAttributeName(resourceAttributeName);
        request.setResourceAttributeMap(resourceAttributeMap);
        request.setShowDeleted(showDeleted);
        return request;
    }
}
