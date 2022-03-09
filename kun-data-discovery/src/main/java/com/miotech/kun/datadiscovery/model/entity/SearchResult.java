package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/18
 */
@Data
public class SearchResult {

    private List<SearchedInfo> searchedInfoList = new ArrayList<>();

    public void add(SearchedInfo searchedInfo) {
        searchedInfoList.add(searchedInfo);
    }
}
