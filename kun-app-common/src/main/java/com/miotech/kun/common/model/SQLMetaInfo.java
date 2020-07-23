package com.miotech.kun.common.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author: Jie Chen
 * @created: 2020/7/20
 */
@Data
public class SQLMetaInfo {

    List<String> tables;

    Set<String> columns;

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public Set<String> getColumns() {
        return columns;
    }

    public void setColumns(Set<String> columns) {
        this.columns = columns;
    }
}
