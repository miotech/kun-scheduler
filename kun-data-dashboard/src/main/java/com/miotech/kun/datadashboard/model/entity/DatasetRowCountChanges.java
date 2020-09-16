package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/9/17
 */
@Data
public class DatasetRowCountChanges {

    List<DatasetRowCountChange> rowCountChanges = new ArrayList<>();

    public void add(DatasetRowCountChange rowCountChange) {
        this.rowCountChanges.add(rowCountChange);
    }
}
