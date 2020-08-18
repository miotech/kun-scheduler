package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataSourcePage extends PageInfo {

    private List<DataSource> datasources;
}
