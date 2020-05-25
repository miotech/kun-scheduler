package com.miotech.kun.datadiscover.model.vo;

import com.miotech.kun.datadiscover.model.PageInfo;
import com.miotech.kun.datadiscover.model.entity.DatasetBasic;
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
public class DatasetListVO extends PageInfo {
    private List<DatasetBasic> datasets;
}
