package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.entity.Glossary;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.GlossaryResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MockSearchInfoFactory {


    private MockSearchInfoFactory() {
    }


    public static SearchedInfo mockDataSetSearch(String keyword) {
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("default")
                .withDatasource("hive")
                .withType("hive")
                .withTags("tag1")
                .withOwners("admin")
                .withSchema("public")
                .build();
        SearchedInfo datasetSearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(IdGenerator.getInstance().nextId())
                .withResourceType(ResourceType.DATASET)
                .withName(keyword)
                .withDescription("desc")
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false).build();
        return datasetSearchedInfo;
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


}
