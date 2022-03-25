package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.common.model.PageInfo;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/18
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchPage extends PageInfo {

    private List<SearchedInfo> searchedInfoList = new ArrayList<>();

    public void add(SearchedInfo searchedInfo) {
        searchedInfoList.add(searchedInfo);
    }
}
