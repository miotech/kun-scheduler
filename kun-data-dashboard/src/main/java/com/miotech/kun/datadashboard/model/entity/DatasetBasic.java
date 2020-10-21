package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/17
 */
@Data
public class DatasetBasic {

    Long gid;

    String datasetName;

    String database;

    String dataSource;
}
