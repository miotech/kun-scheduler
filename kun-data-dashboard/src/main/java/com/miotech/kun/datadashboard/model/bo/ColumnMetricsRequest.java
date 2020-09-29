package com.miotech.kun.datadashboard.model.bo;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Chen
 * @created: 2020/9/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ColumnMetricsRequest extends PageInfo {

    String sortColumn = "columnNullCount";

    String sortOrder = "desc";
}
