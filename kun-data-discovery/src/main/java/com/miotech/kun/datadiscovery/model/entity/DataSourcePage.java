package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.common.model.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourcePage extends PageInfo {

    private List<DataSource> datasources;
}
