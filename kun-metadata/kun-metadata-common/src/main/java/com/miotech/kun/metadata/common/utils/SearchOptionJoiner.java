package com.miotech.kun.metadata.common.utils;

import com.miotech.kun.metadata.core.model.constant.SearchOperator;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: kun
 * @description: Search Option Joiner
 * @author: zemin  huang
 * @create: 2022-03-11 17:56
 **/
public final class SearchOptionJoiner {
    private StringBuilder value;

    public SearchOptionJoiner add(SearchFilterOption searchFilterOption) {
        String keyword = escapeSql(searchFilterOption.getKeyword());
        if (StringUtils.isBlank(keyword)){
            return this;
        }
        String searchContentsString = searchFilterOption.getSearchContentsWeightString();
        prepareBuilder(searchFilterOption.getSearchOperator()).append(keyword).append(":*").append(searchContentsString);
        return this;
    }

    public SearchOptionJoiner add(List<SearchFilterOption> searchFilterOptionList) {
        if (CollectionUtils.isEmpty(searchFilterOptionList)) {
            return this;
        }
        searchFilterOptionList.forEach(this::add);
        return this;
    }
    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    private StringBuilder prepareBuilder(SearchOperator searchOperator) {
        if (value != null) {
            value.append(searchOperator.getOperator());
        } else {
            value = new StringBuilder();
        }
        return value;
    }

    public String toString() {
        if (Objects.isNull(value)) {
            return "";
        }
        return value.toString();
    }

}
