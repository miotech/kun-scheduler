package com.miotech.kun.datadiscovery.model.vo;

import com.miotech.kun.common.model.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/22
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataSourceBasicPage extends PageInfo {

    List<DatasourceBasicVO> datasources = new ArrayList<>();


    public void add(DatasourceBasicVO datasourceBasic) {
        datasources.add(datasourceBasic);
    }
}
