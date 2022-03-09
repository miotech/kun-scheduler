package com.miotech.kun.metadata.core.model.vo;


import com.miotech.kun.metadata.core.model.search.SearchedInfo;

import java.io.Serializable;
import java.util.List;

/**
 * @program: kun
 * @description: Universal Search Info
 * @author: zemin  huang
 * @create: 2022-03-08 10:16
 **/
public class UniversalSearchInfo implements Serializable {
    private List<SearchedInfo> searchedInfoList;

    public List<SearchedInfo> getSearchedInfoList() {
        return searchedInfoList;
    }

    public void setSearchedInfoList(List<SearchedInfo> searchedInfoList) {
        this.searchedInfoList = searchedInfoList;
    }
}
