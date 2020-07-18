package com.miotech.kun.commons.query.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class QueryResultSet {

    private List<Map<String, ?>> resultSet = new ArrayList<>();

    public void addRow(Map<String, ?> map) {
        resultSet.add(map);
    }

    public List<Map<String, ?>> getResultSet() {
        return resultSet;
    }
}
