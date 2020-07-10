package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/22
 */
@Data
public class BaseSearchPage<T> {

    private List<T> searchEntities = new ArrayList<>();

    public void add(T entity) {
        searchEntities.add(entity);
    }
}
