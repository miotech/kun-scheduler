package com.miotech.kun.commons.query;

/**
 * @author: Jie Chen
 * @created: 2020/7/8
 */
public interface Query<T extends QueryEntry> {

    QuerySite getQuerySite();

    T getQueryEntry();
}
