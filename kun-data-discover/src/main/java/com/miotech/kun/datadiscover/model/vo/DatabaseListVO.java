package com.miotech.kun.datadiscover.model.vo;

import com.miotech.kun.datadiscover.model.PageInfo;
import com.miotech.kun.datadiscover.model.entity.Datasource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class DatabaseListVO extends PageInfo {

    private List<Datasource> datasources;

    private String sortColumn;

    private String sortOrder;
}
