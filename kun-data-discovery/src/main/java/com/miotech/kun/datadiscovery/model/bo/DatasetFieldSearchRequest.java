package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.datadiscovery.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Chen
 * @created: 2020/6/29
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DatasetFieldSearchRequest extends PageInfo {

    private String keyword;

}
