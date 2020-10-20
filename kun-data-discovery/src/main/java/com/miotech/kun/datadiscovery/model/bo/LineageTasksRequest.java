package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class LineageTasksRequest {

    Long datasetGid;

    String direction;

    Long sourceDatasetGid;

    Long destDatasetGid;
}
