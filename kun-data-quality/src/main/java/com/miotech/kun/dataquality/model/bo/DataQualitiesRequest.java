package com.miotech.kun.dataquality.model.bo;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataQualitiesRequest extends PageInfo {

    Long gid;
}
