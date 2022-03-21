package com.miotech.kun.metadata.core.model.vo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: search Dao
 * @author: zemin  huang
 * @create: 2022-03-08 10:16
 **/

public class UniversalSearchRequest  extends PageInfo implements Serializable {
    private List<SearchFilterOption> searchFilterOptions = Lists.newArrayList();
    private Set<ResourceType> resourceTypes= Sets.newHashSet(ResourceType.values());


    public List<SearchFilterOption> getSearchFilterOptions() {
        return searchFilterOptions;
    }

    public Set<ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(Set<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public Set<String> getResourceTypeNames() {
        return resourceTypes.stream().map(Enum::name).collect(Collectors.toSet());
    }
    public void setSearchFilterOptions(List<SearchFilterOption> searchFilterOptions) {
        this.searchFilterOptions = searchFilterOptions;
    }
}
