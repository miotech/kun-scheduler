package com.miotech.kun.datadiscovery.model.bo;

import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/11/4
 */
@Data
public class DatabaseRequest {

    List<Long> dataSourceIds;
}
