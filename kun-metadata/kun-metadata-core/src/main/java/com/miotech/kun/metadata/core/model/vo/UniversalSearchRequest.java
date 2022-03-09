package com.miotech.kun.metadata.core.model.vo;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchOperator;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @program: kun
 * @description: search Dao
 * @author: zemin  huang
 * @create: 2022-03-08 10:16
 **/

public class UniversalSearchRequest implements Serializable {
    private List<SearchFilterOption> searchFilterOptions = Lists.newArrayList();
    private ResourceType[] resourceTypes=ResourceType.values();
    private Integer limitNum = 100;


    public List<SearchFilterOption> getSearchFilterOptions() {
        return searchFilterOptions;
    }

    public ResourceType[] getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(ResourceType[] resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public void setSearchFilterOptions(List<SearchFilterOption> searchFilterOptions) {
        this.searchFilterOptions = searchFilterOptions;
    }

    public Integer getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(Integer limitNum) {
        this.limitNum = limitNum;
    }
}
